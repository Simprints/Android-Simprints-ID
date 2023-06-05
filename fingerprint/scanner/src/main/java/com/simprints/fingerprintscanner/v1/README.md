# Fingerprint Scanner V1

This package contains the code for interfacing with Vero 1. It has
remained largely unchanged since when it was originally written in Java.

The main point of entry is [Scanner.java](./Scanner.java), though it
relies on the peripheral
[Component abstractions](../component/bluetooth) for some handling of
the connection with the scanner.

Calling functions on the `Scanner` is done through callbacks. Exceptions
or issues are converted to a [`SCANNER_ERROR`](./SCANNER_ERROR.java)
which is propogated through the callback. Some light Kotlin wrapping has
been introduced to make calling the methods with lambdas possible.

The [`Message`](./Message.java) class provides utility for both parsing
messages from received bytes and serializing messages to send into
bytes. [`MessageDispatcher`](./MessageDispatcher.java) handles the
sending of commands and awaiting of responses.

Note that there is some code referencing Over The Air updates (OTA) in
this package - this relies on firmware that is unlikely to ever be
released and can be ignored. Vero 1 is currently incapable of updating
via OTA.
