# Project 2

Demo: **1 server, 3 clients**

## Connection

Type `%connect 127.0.0.1 55555`
    - 127.0.0.1 is ip_address
    - 55555 is port

## part 1

- all three users
    `%join`
- one of user:
    `%post;Greeting;What's up guys!`
- all three users
    `%leave`

## part 2

- user1:
    `%groupjoin;1`
- user2:
    `%groupjoin;2`
- user3:
    `%groupjoin;2`
- user1:
    `%post;Greeting;What's up guys!`
- user2:
    `%post;Greeting;What's up guys!`
- user2:
    `%groupjoin;3`
- user2:
    `%grouppost;2;CSE3461;Computer networking`
- user2:
    `%grouppost;1;CSE3461;Computer networking`

## leave groups and exit

```sh
user1:%exit
user2:%exit
user3:%exit
```
