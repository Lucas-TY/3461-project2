import socket
import select

HEADER_LENGTH = 10
IP = '127.0.0.1'
PORT = 1234
#create server_socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# allow reuse local address
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

server.bind((IP, PORT))
server.listen()
#manage list of client
socket_list = [server]
clients = {}

def receive_message(client):
    # receive message header
    header = client.recv(HEADER_LENGTH)
    if not len(header):
        return False
    # get the length of the message
    length = int (header.decode('utf-8'))
    return {'header': header, 'data': client.recv(length)}

while True:
    #read list, write list, and exception list
    read, _, exceptions = select.select(socket_list,[], socket_list)
    for element in read:
        # if it is the server, receive message from clients
        if element == server:
            client, client_address = server.accept()
            user = receive_message(client)

            if user is False:
                continue
            #add to the user list
            socket_list.append(client)
            # add a client to the client dic
            clients[client] = user
            print(f"New user:{user['data'].decode('utf-8')} is connected from {client_address[0]}:{client_address[1]}.")
        else:
            message = receive_message(element)
            if message is False:
                data= clients[element]['data'].decode('utf-8')
                print(f'Closed connection from {data}')
                del clients[element]
            
            user = clients[element]
            print(f"Received message from {user['data'].decode('utf-8')}:{message['data'].decode('utf-8')} ")
            #share the message to everyone
            for client in clients:
                if client != element:
                    client.send(user['header']+user['data']+message['header']+message['data'])
    # Handling exceptions
    for element in exceptions:
        socket_list.remove(element)
        del clients[element]
