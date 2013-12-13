# deposit-update-message-handler

Handle DOI deposit messages from the message bus.

## Usage

    $ java -jar deposit-update-message-handler-0.1.0-standalone.jar [args]

## Options

Options set using environment variables. In your lein profile file include lein-environ plugin and set:

    :env {
        :mongo-db-name "crossref"
        :email-username "labs-notifications"
        :email-password ...              
    }

When running, ensure that these are set as MONGO_DB_NAME etc.

### Bugs

...

## TODO

Schedule

## License

Copyright © 2013 CrossRef

