mTLS Heartbeat & UDP Broadcast System
This project is a high-security communication ecosystem demonstrating Mutual TLS (mTLS) authentication, asynchronous UDP binary networking, and containerized microservices. It is designed with certificate-based identity verification and high-precision event tracking, suitable for senior backend evaluation.

System Architecture
The solution consists of three primary components:
1.	mTLS Server (Java/Spring Boot)
o	Terminates mTLS connections
o	Extracts identity from X.509 certificates (CN as email)
o	Persists User data to PostgreSQL
o	Broadcasts user updates via UDP
2.	mTLS Client (Java/Spring Boot)
o	CLI utility that establishes a secure handshake using a CA-signed PKCS12 identity
o	Sends an empty HTTP PATCH /heartbeat request over mTLS
o	Runnable JAR provided for evaluator convenience
3.	Network Listener (Python)
o	Listens for UDP broadcasts on port 6667
o	Decodes binary messages into email, lastSeen, IP, and port
o	Gracefully discards malformed packets


 Execution Guide 
1. Prerequisites
•	Docker & Docker Compose
•	Java 17 (for client JAR execution)
•	Python (for listener script)

2. Launch the Server & Database
cd mtls-server
docker-compose up --build -d
•	The default Dockerfile builds the JAR inside Docker.
•	First-time build may take a few minutes to download Maven dependencies.

3. Optional: Faster Startup Using Local JAR
If you want to avoid long Maven download times:
1.	Build the JAR locally:
cd mtls-server
mvn clean package -DskipTests
2.	Uncomment the USE LOCAL JAR section in the Dockerfile 
3.	Then run Docker Compose as usual:
docker-compose up --build -d
This starts immediately but is less "pure" than building inside Docker

4. Seed Test Data (Required)
The server enforces a Lookup Only policy. To prevent 403 Forbidden, register the email from the client certificate:
docker exec -it mtls-postgres psql -U mtls_user -d mtls_db \ -c "INSERT INTO users (email) VALUES ('ntezimanagad10@gmail.com');"

5. Run the UDP Listener
In a separate terminal (root project directory):
python udp-listener.py


6. Run the Client JAR
Option 1 Download Pre-Built JAR (Recommended)
1.	Download the pre-built client JAR from the GitHub Releases section:
2.	mtls-client-0.0.1-SNAPSHOT.jar
3.	Run the JAR:
4.	java -jar mtls-client-0.0.1-SNAPSHOT.jar
•	Sends an empty PATCH request to https://localhost:8443/heartbeat over mTLS
•	If successful, the server updates the database and broadcasts a UDP packet

Option 2  Build Locally
1.	Build the client locally:
2.	cd mtls-client
3.	mvn clean package -DskipTests
4.	Run the newly built JAR:
5.	java -jar target/mtls-client-0.0.1-SNAPSHOT.jar
Technical Implementation Details
Mutual TLS Security
•	Self-Signed CA: A custom Root CA was used to sign server and client certificates.
•	Certificate Validation: Server requires client-auth: need. Invalid or missing certificates return 403 Forbidden.
•	Identity Extraction: CN from client certificate is extracted via jakarta.servlet.request.X509Certificate.
Precision Event Tracking
•	lastSeen is stored in nanoseconds since Unix epoch (Java Instant.now()).
Binary UDP Protocol
•	Broadcasts on port 6666 → listener on port 6667
•	Custom binary buffer (Big-Endian) format:
[EmailLength(4b)][Email(UTF8)][LastSeen(8b)][IPLength(4b)][IP(UTF8)][Port(4b)]
•	Python listener converts lastSeen to human-readable timestamps
________________________________________
Project Structure
C:.
├── certs/                 
│   ├── ca/
│   ├── client/
│   └── server/
├── mtls-client/           
│   └── target/
│       └── mtls-client-0.0.1-SNAPSHOT.jar
├── mtls-server/           
│   └── src/main/resources/certs/
└── udp-listener.py       

Candidate: Ntezimana Gad
Position: Senior Backend Engineer Assessment
Repository: [GitHub Link]
