import socket
import threading
import sys
import json

# get user name from user.
username = input("Give a username: ")
# get command from user.
command = input("connect to server:")
# change the string to list
arguments = command.split()
if arguments[0] == 'connect':
    if len(arguments)!=3:
        print("Connect needs two arguments: address, port ")
        sys.exit()
    # connect to server
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((arguments[1], int(arguments[2])))
    # receive message from server.
    def receive():
        while True:
            try:
                # Receive Message From Server
                message = client_socket.recv(1024).decode('ascii')
                print(message)
            except:
                # Close Connection When Error
                print("An error occured!")
                client_socket.close()
                break
    # Change the input string to json string
    def toJsonString(name, input):
        arguments = input.split(';')
        dictionary ={}
        dictionary[0] = name
        for i in range(len(arguments)):
            dictionary[i+1] = arguments[i]
        json_object = json.dumps(dictionary)
        return json_object
    # Sending Messages To Server
    def write():
        while True:
            message = '{}: {}'.format(username, toJsonString(username, input('')))
            client_socket.send(message.encode('ascii'))

    # Starting Threads For Listening And Writing
    receive_thread = threading.Thread(target=receive)
    receive_thread.start()

    write_thread = threading.Thread(target=write)
    write_thread.start()
