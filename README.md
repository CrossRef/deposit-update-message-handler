# deposit-update-message-handler

Handle DOI deposit messages from an email address, update the mongo deposits datastore. This will check the email address on a schedule. If it finds an email it can understand (simple or multi-part) it will try to parse it and update Mongo. If this succeeds, it will delete the email. If it fails it will not delete the email.

Set configuration options first, then run in a background process, e.g. tmux.

## Usage

    $ java -jar deposit-update-message-handler-0.1.0-standalone.jar

or

    $ lein run

or in production

    $ lein daemon start deposit-update-message-handler

Here is a sample run script you can keep on the server

    export MONGO_DB_NAME=crossref
    export EMAIL_USERNAME=labs-notifications
    export EMAIL_PASSWORD=...
    lein run

## Options

Options set using environment variables. In your lein profile file or `./.lein-env` include lein-environ plugin and set:

    :env {
        :mongo-db-name "crossref"
        :email-username "labs-notifications"
        :email-password ...              
    }

When running, ensure that these are set as MONGO_DB_NAME etc.

## License

Copyright © 2013-2014 CrossRef

