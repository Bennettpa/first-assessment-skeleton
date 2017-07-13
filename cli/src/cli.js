import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

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
      this.log(mes.toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /\S*/g)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command, contents: ' has disconnected' }).toJSON() + '\n')
    } else if (command === 'echo' || command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.match(/^@\S*/g)) {
      server.write(new Message({ username, command: 'whisper', contents: command.replace('@', '') + contents }).toJSON() + '\n')
    } else if (command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
