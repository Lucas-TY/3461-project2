# 3461 PROGRAMMING ASSIANGMENT 2
# Team member: Leon Cai & Lucas wu
# How to compile and run our program:
1. change direction to current folfer
2. run the server by typing 
3. run the client by typing py client.py in a separate command window
4. give command in the command window of the client.py
5. use connect 127.0.0.1 55555 to connect to server
 for other commands, type: commandName; param1; param2 to run the command, each argument is separated by ";"
    avaliable commands:
    connect 127.0.0.1 55555
    join
    post; subject; content
    users
    leave
    message; messageId
    exit

for part 2, avaliable commands are:
    groups
    groupjoin; groupId
    grouppost; groupId; subject; content
    groupusers; groupId
    groupleave; groupId
    groupmessage; groupId; messageId
