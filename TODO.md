TODO:

# Core functionality

* Domain layer
  * text message 
  * button sealed trait

* Router actor
  * translating Telegram API messages to domain objects
  * creating one user actor per user id

* User actor
  * buffering incoming messages
  * replacing message with specified id with edited variant
  * performing actions reacting to buttons

* Pusher actor
  * translating incoming commands and sending messages to Telegram API

* In-memory persistence layer
  * For testing purposes

* Cassandra persistence layer
  * Storing domain items
  * using akka-persistence for message storing and retrieving
  
# Aux functionality

* Context items
* User profile settings (for date items and location items)
* Date items
* Time items
* Location items
