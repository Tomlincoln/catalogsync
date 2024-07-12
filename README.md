# Catalog Syncer

### Guide
1. mvn clean install 
2. Copy file1.txt file2.txt and file3.txt to target dir
3. Start with java -jar from target dir

### Usage
You can use the following endpoints:
- http://localhost:8080/sync/1 (sync based on file1.txt)
- http://localhost:8080/sync/2 (sync based on file2.txt)
- http://localhost:8080/sync/3 (sync based on file3.txt)
- http://localhost:8080/dump (dumps db to JSON)

