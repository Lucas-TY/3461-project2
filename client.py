import socket
import threading
import sys
import json
import tkinter
import tkinter.scrolledtext
from tkinter import simpledialog
class Client:
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # ask for user name
        msg = tkinter.Tk()
        msg.withdraw()
        self.username = simpledialog.askstring('Username', 'Please choose a username', parent = msg)
        #ask for connect command
        msg = tkinter.Tk()
        msg.withdraw()
        self.arguments = simpledialog.askstring('connect', 'Please connect to server using %' +'connect', parent = msg)
        #connect to server
        self.command = self.arguments.split()
        self.sock.connect((self.command[1],int(self.command[2])))
        self.gui_done = False
        self.running = True
        gui_thread = threading.Thread(target = self.gui_loop)
        receive_thread = threading.Thread(target = self.receive)

        gui_thread.start()
        receive_thread.start()
    
    def gui_loop(self):
        self.win = tkinter.Tk()
        self.win.configure(bg='lightgray')

        self.chat_label = tkinter.Label(self.win, text = 'Chat:', bg='lightgray')
        self.chat_label.config(font=('Arial', 12))
        self.chat_label.pack(padx=20, pady=5)

        self.text_area = tkinter.scrolledtext.ScrolledText(self.win)
        self.text_area.pack(padx =20, pady=5)
        self.text_area.config(state='disabled')

        self.msg_label = tkinter.Label(self.win, text = 'Message:', bg='lightgray')
        self.msg_label.config(font=('Arial', 12))
        self.msg_label.pack(padx=20, pady=5)

        self.input_area = tkinter.Text(self.win, height = 3)
        self.input_area.pack(padx=20, pady=5)
        self.send_button = tkinter.Button(self.win, text = 'Send', command = self.write)
        self.send_button.config(font=('Arial', 12))
        self.send_button.pack(padx=20, pady=5)
        self.gui_done = True
        self.win.protocol('WM_DELETE_WINDOW', self.stop)
        self.win.mainloop()

    def write(self):
        message = '{}'.format(self.toJsonString(self.username, self.input_area.get('1.0','end-1c')))
        self.sock.send(message.encode('ascii'))
        self.input_area.delete('1.0', 'end')
    def receive(self):
       while self.running:
            try:
                message = self.sock.recv(1024).decode('ascii')
                # Receive Message From Server
                if self.gui_done:
                    self.text_area.config(state = 'normal')
                    self.text_area.insert('end', message)
                    self.text_area.yview('end')
                    self.text_area.config(state = 'disabled')
            except:
                # Close Connection When Error
                print("An error occured!")
                self.sock.close()
                break
    def stop(self):
        self.running = False
        self.win.destroy()
        self.sock.close()
        exit(0)
    # change commands to jason string
    def toJsonString(self, name, input):
        arguments = input.split(';')
        dictionary ={}
        dictionary[0] = name
        for i in range(len(arguments)):
            dictionary[i+1] = arguments[i]
        json_object = json.dumps(dictionary)
        return json_object

client = Client()  

