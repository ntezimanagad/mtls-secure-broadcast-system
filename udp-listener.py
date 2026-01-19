import socket
import struct
import datetime

LISTEN_PORT = 6667
BUFFER_SIZE = 4096

def nanos_to_datetime(nanos):
    try:
        seconds = nanos / 1_000_000_000
        return datetime.datetime.utcfromtimestamp(seconds)
    except Exception:
        return "Invalid timestamp"

def parse_packet(data):
    offset = 0

    email_len = struct.unpack_from(">I", data, offset)[0]
    offset += 4
    email = data[offset:offset + email_len].decode("utf-8")
    offset += email_len

    last_seen = struct.unpack_from(">Q", data, offset)[0]
    offset += 8

    ip_len = struct.unpack_from(">I", data, offset)[0]
    offset += 4
    ip = data[offset:offset + ip_len].decode("utf-8")
    offset += ip_len

    port = struct.unpack_from(">I", data, offset)[0]

    return email, last_seen, ip, port

def main():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(("", LISTEN_PORT))

    print(f"Listening for UDP broadcasts on port {LISTEN_PORT}...\n")

    while True:
        try:
            data, addr = sock.recvfrom(BUFFER_SIZE)
            source_ip, source_port = addr

            email, last_seen, ip, port = parse_packet(data)

            print(" Packet received")
            print(f"   Email     : {email}")
            print(f"   Last Seen : {nanos_to_datetime(last_seen)}")
            print(f"   IP        : {ip}")
            print(f"   Port      : {port}")
            print(f"   Source    : {source_ip}:{source_port}")
            print("-" * 50)

        except Exception:
            continue

if __name__ == "__main__":
    main()
