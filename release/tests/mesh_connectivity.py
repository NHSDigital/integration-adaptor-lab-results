import json
import subprocess
import sys
import time


class MeshClient:
    MESH_SCRIPT = './mesh.sh'

    def __init__(self, mesh_script_path, env=None):
        self.mesh_script_path = mesh_script_path
        self.env = env

    def send(self, mailbox, edifact):
        result = self._invoke_command('send', mailbox, edifact)
        parsed_result = json.loads(result)
        if 'messageID' not in parsed_result:
            raise Exception("MESH send command returned unexpected result: " + result)

    def get_message_ids(self, mailbox):
        result = self._invoke_command('inbox', mailbox)
        return json.loads(result)['messages']

    def download_message(self, mailbox, message_id):
        result = self._invoke_command('download', mailbox, message_id)
        if not result:
            raise Exception("Downloading message " + message_id + " yield no result")
        return result

    def ack_message(self, mailbox, message_id):
        result = self._invoke_command('ack', mailbox, message_id)
        if not result.startswith('HTTP/1.1 200 OK'):
            raise Exception("MESH ack command returned unexpected result: " + result)

    def _invoke_command(self, *args):
        cmd = [self.MESH_SCRIPT] + list(args)

        result = subprocess.run(cmd, cwd=self.mesh_script_path, capture_output=True, text=True, env=self.env)

        if result.stderr:
            raise Exception(result.stderr)
        return result.stdout


class MeshConnectivityTestRunner:

    def __init__(self, mesh_client, mailbox, ack_mailbox, wait_time):
        self.mesh_client = mesh_client
        self.mailbox = mailbox
        self.ack_mailbox = ack_mailbox
        self.wait_time = wait_time

    @staticmethod
    def is_interchange_nhsack(edifact, interchange_details):
        # NIR\+SIR:1015\+000000004400001:80\+000000024600002:80\+IAP'
        # NMR\+AGO:1\+MA'

        segments = edifact.split("'")
        interchange_header_fields = list(filter(lambda s: s.startswith('UNB'), segments))[0].split('+')

        if interchange_header_fields[7] != 'NHSACK':
            return False

        nhsack_sender = interchange_header_fields[2].split(':')[0]
        nhsack_recipient = interchange_header_fields[3].split(':')[0]
        nhsack_nir_segment_fields = list(filter(lambda s: s.startswith('NIR'), segments))[0].split('+')
        nhsack_message_ids = list(map(lambda f: f[1].split(':')[1], map(lambda s: s.split('+'), filter(lambda s: s.startswith('NMR'), segments))))

        return nhsack_sender == interchange_details['recipient'] == nhsack_nir_segment_fields[3].split(':')[0] \
            and nhsack_recipient == interchange_details['sender'] == nhsack_nir_segment_fields[2].split(':')[0] \
            and nhsack_nir_segment_fields[1].split(':')[1] == interchange_details['interchange_id'] \
            and nhsack_message_ids == interchange_details['message_ids']

    @staticmethod
    def read_interchange_details(edifact):
        # UNB+UNOC:3+000000004400001:80+000000024600002:80+100301:1751+1015++MEDRPT++1'
        # UNH+1+MEDRPT:0:1:RT:NHS003'

        segments = edifact.split("'")

        interchange_header_fields = list(filter(lambda s: s.startswith('UNB'), segments))[0].split('+')

        if len(interchange_header_fields) < 10 or interchange_header_fields[9] != '1':
            raise Exception("Test file must have 'request nhsack' flag set to '1'")

        message_fields = list(map(lambda f: f[1], map(lambda s: s.split('+'), filter(lambda s: s.startswith('UNH'), segments))))

        return {
            "sender": interchange_header_fields[2].split(':')[0],
            "recipient": interchange_header_fields[3].split(':')[0],
            "interchange_id": interchange_header_fields[5],
            "message_ids": message_fields
        }

    @staticmethod
    def read_file(file_path):
        with open(file_path, 'r') as file:
            return file.read().replace('\n', '')

    def test(self, edifact_file_path):
        edifact = self.read_file(edifact_file_path)
        interchange_details = self.read_interchange_details(edifact)

        print("Cleaning existing matching NHSACK")
        for message_id in self._get_matching_nhsack_message_ids(interchange_details):
            print("Sending ACK for message " + message_id)
            self.mesh_client.ack_message(self.ack_mailbox, message_id)

        print("Sending EDIFACT")
        self.mesh_client.send(self.mailbox, edifact)

        print("Sleeping for " + str(self.wait_time) + " seconds")
        time.sleep(float(self.wait_time))

        print("Checking mailbox for matching NHSACK")
        matching_nhsack_message_ids = self._get_matching_nhsack_message_ids(interchange_details)
        if not matching_nhsack_message_ids:
            raise Exception("Matching NHSACK not found")
        else:
            print("Test passed!!! Found " + str(len(matching_nhsack_message_ids)) + " matching NHSACK")
            for nhsack_message_id in matching_nhsack_message_ids:
                print("Sending ACK for message " + nhsack_message_id)
                self.mesh_client.ack_message(self.ack_mailbox, nhsack_message_id)

    def _get_matching_nhsack_message_ids(self, interchange_details):
        message_ids = self.mesh_client.get_message_ids(self.ack_mailbox)
        matching_nhsack_message_ids = []
        for message_id in message_ids:
            message = self.mesh_client.download_message(self.ack_mailbox, message_id)
            if self.is_interchange_nhsack(message, interchange_details):
                matching_nhsack_message_ids.append(message_id)
        return matching_nhsack_message_ids


if __name__ == '__main__':
    _mesh_script_path = sys.argv[1]
    _mailbox = sys.argv[2]
    _ack_mailbox = sys.argv[3]
    _wait_time = sys.argv[4]
    _test_file = sys.argv[5]
    _test_runner = MeshConnectivityTestRunner(MeshClient(_mesh_script_path), _mailbox, _ack_mailbox, _wait_time)
    _test_runner.test(_test_file)
