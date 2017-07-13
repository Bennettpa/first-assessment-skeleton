import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let lastcommand = ' '

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    let host = args.host | 'localhost'
    let port = args.port | 8080
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect', contents: ' has connected' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      let mes = Message.fromJSON(buffer)
      if (mes.command === 'broadcast') {
        this.log('\x1b[36m' + mes.toString())
      } else if (mes.command === 'echo') {
        this.log('\x1b[35m' + mes.toString())
      } else if (mes.command.match(/^@\S*/g)) {
        this.log('\x1b[37m' + mes.toString())
      } else if (mes.command === 'users') {
        this.log('\x1b[34m' + mes.toString())
      } else {
        this.log('\x1b[31m' + mes.toString())
      }
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input, /\S*/g)
    let contents = rest.join(' ')
    if (command === 'disconnect') {
      server.end(new Message({ username, command, contents: ' has disconnected' }).toJSON() + '\n')
    } else if (command === 'echo' || command === 'broadcast') {
      lastcommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.match(/^@\S*/g)) {
      lastcommand = command
      server.write(new Message({ username, command: command, contents: command.replace('@', '') + contents }).toJSON() + '\n')
    } else if (command === 'users') {
      lastcommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (lastcommand !== ' ') {
      contents = ' ' + command + ' ' + contents
      command = lastcommand
      if (command === 'disconnect') {
        server.end(new Message({ username, command, contents: ' has disconnected' }).toJSON() + '\n')
      } else if (command === 'echo' || command === 'broadcast') {
        lastcommand = command
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } else if (command.match(/^@\S*/g)) {
        lastcommand = command
        server.write(new Message({ username, command: command, contents: command.replace('@', '') + contents }).toJSON() + '\n')
      } else if (command === 'users') {
        lastcommand = command
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      }
    } else {
      this.log(`Command <${command}> was not recognized. A command is required`)
    }

    callback()
  })
