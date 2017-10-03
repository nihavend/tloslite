type README.txt
@echo off
Chcp 1254
java -Duser.country=ENTR -Duser.language=entr -jar tloslite-1.8.2.jar -cfg tlos-config-win.xml 2> error.log
		