# 3461 PROGRAMMING ASSIANGMENT 2

# Team member 

```
Leon Cai & Lucas wu
```

# How to compile and run our program:

1. change direction to current folfer
2. run the server by typing `java server/Server.java 55555`in complile_java folder (55555 is the port number)
3. run the client by typing `py client.py` in a separate command window
4. give command in the command window of the client.py
5. use `connect 127.0.0.1 55555` to connect to server 
   - `55555` is the port number
   - `127.0.0.1` is the local ip address
# GUI Client
- `py testClient.py`
# Command   
**for other commands, type: commandName; param1; param2 to run the command, each argument is separated by `;`**

- avaliable commands for part 1:
  ```
   %connect 127.0.0.1 55555
   %join
   %post;subject;content
   %users
   %leave
   %message;messageId
   %exit
  ```
- avaliable commands for part 2:
  ```
   %groups
   %groupjoin;groupId
   %grouppost;groupId;subject;content
   %groupusers;groupId
   %groupleave;groupId
   %groupmessage;groupId;messageId
  ```
  
  <br/>

# Design flaws:

1. use split(;) to separate arguments from user input to ensure that the subject and body can have space,
 which limited the format of user input in another way
