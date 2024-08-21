1. Install IBM Lotus Notes client. It is required for loading the core dlls during runtime. 

2. Add "C:\Program Files (x86)\IBM\Notes" to the Path (Environment Variables) so that nlsxbe.dll can be referenced during runtime.

3. Use 32-bit JDK because nlsxbe.dll is a 32-bit dll which can't be loaded in 64-bit environment.
	https://stackoverflow.com/questions/27868284/java-lang-unsatisfiedlinkerror-c-program-files-x86-ibm-lotus-notes-nlsxbe-dl
	https://www.ibm.com/docs/en/sdi/7.2.0.2?topic=connectors-ncsojar-file

4. Install Notes.jar from IBM designated location to the local repository:
   mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="C:\Program Files (x86)\IBM\Notes\jvm\lib\ext\Notes.jar" -DgroupId="com.ibm" -DartifactId="Notes" -Dversion="1.0"  -Dpackaging=jar

5. Add the following dependencies to the POM.xml (if missing): glassfish-corba-omgapi is required because Notes is referencing it internally.
	<dependency>
		<groupId>com.ibm</groupId>
		<artifactId>Notes</artifactId>
		<version>1.0</version>
	</dependency>
	<dependency>
		<groupId>org.glassfish.corba</groupId>
		<artifactId>glassfish-corba-omgapi</artifactId>
		<version>4.2.3</version>
	</dependency>

6. Using Java API to read Lotus Notes documents - https://stackoverflow.com/questions/7850189/how-do-i-get-all-the-attachments-from-a-nsflotus-notes-file-using-java/7854740

7. Edit access is needed for creating documents / uploading files.