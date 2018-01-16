package com.github.alextokarew.telegram.bots.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}

/**
  * Created by alextokarew on 30.08.16.
  */
trait Protocol extends SprayJsonSupport with DefaultJsonProtocol {
  import Protocol.Responses._

  implicit val audioFormat: RootJsonFormat[Audio] = jsonFormat6(Audio)
  implicit val chatFormat: RootJsonFormat[Chat] = jsonFormat6(Chat)
  implicit val contactFormat: RootJsonFormat[Contact] = jsonFormat4(Contact)
  implicit val locationFormat: RootJsonFormat[Location] = jsonFormat2(Location)
  implicit val photoSizeFormat: RootJsonFormat[PhotoSize] = jsonFormat4(PhotoSize)
  implicit val documentFormat: RootJsonFormat[Document] = jsonFormat5(Document)
  implicit val stickerFormat: RootJsonFormat[Sticker] = jsonFormat6(Sticker)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)
  implicit val messageEntityFormat: RootJsonFormat[MessageEntity] = jsonFormat5(MessageEntity)
  implicit val inlineQueryFormat: RootJsonFormat[InlineQuery] = jsonFormat5(InlineQuery)
  implicit val userProfilePhotosFormat: RootJsonFormat[UserProfilePhotos] = jsonFormat2(UserProfilePhotos)
  implicit val venueFormat: RootJsonFormat[Venue] = jsonFormat4(Venue)
  implicit val videoFormat: RootJsonFormat[Video] = jsonFormat7(Video)
  implicit val voiceFormat: RootJsonFormat[Voice] = jsonFormat4(Voice)

  /**
    * Explicitly defining JsonFormat for Message case class because it has more than 22 arguments
    */
  implicit val messageFormat: RootJsonFormat[Message]  = new RootJsonFormat[Message] {
    lazy val reader = new MessageReader

    def write(obj: Message): JsValue =
      throw new UnsupportedOperationException("Case class Message can't be serialized to JSON")

    def read(json: JsValue): Message = reader.read(json)
  }

  private[Protocol] class MessageReader {
    val fieldNames: Array[String] = extractFieldNames(classManifest[Message])

    private def forField[T: JsonFormat](v: JsValue, pos: Int): T =
      fromField(v, fieldNames(pos))(implicitly[JsonFormat[T]])

    def read(value: JsValue): Message = Message(
      forField[Long](value, 0),
      forField[Option[User]](value, 1),
      forField[Long](value, 2),
      forField[Chat](value, 3),
      forField[Option[User]](value, 4),
      forField[Option[Chat]](value, 5),
      forField[Option[Long]](value, 6),
      forField[Option[Message]](value, 7),
      forField[Option[Long]](value, 8),
      forField[Option[String]](value, 9),
      forField[Option[Seq[MessageEntity]]](value, 10),
      forField[Option[Audio]](value, 11),
      forField[Option[Document]](value, 12),
      forField[Option[Seq[PhotoSize]]](value, 13),
      forField[Option[Sticker]](value, 14),
      forField[Option[Video]](value, 15),
      forField[Option[Voice]](value, 16),
      forField[Option[String]](value, 17),
      forField[Option[Contact]](value, 18),
      forField[Option[Location]](value, 19),
      forField[Option[Venue]](value, 20),
      forField[Option[User]](value, 21),
      forField[Option[User]](value, 22),
      forField[Option[String]](value, 23),
      forField[Option[Seq[PhotoSize]]](value, 24),
      forField[Option[Boolean]](value, 25),
      forField[Option[Boolean]](value, 26),
      forField[Option[Boolean]](value, 27),
      forField[Option[Boolean]](value, 28),
      forField[Option[Long]](value, 29),
      forField[Option[Long]](value, 30),
      forField[Option[Message]](value, 31)
    )
  }

  implicit val callbackQueryFormat: RootJsonFormat[CallbackQuery] = jsonFormat5(CallbackQuery)
  implicit val chosenInlineResultFormat: RootJsonFormat[ChosenInlineResult] = jsonFormat5(ChosenInlineResult)
  implicit val updateFormat: RootJsonFormat[Update] = jsonFormat6(Update)

  /**
    * Explicitly defining JsonFormat for OkWrapper because it has type parameter.
    */
  implicit def okWrapperFormat[T : JsonFormat]: OkWrapperFormat[T] = new OkWrapperFormat[T]

  private[Protocol] class OkWrapperFormat[T : JsonFormat] extends RootJsonFormat[OkWrapper[T]] {
    override def write(obj: OkWrapper[T]): JsValue =
      throw new UnsupportedOperationException("Case class OkWrapper can't be serialized to JSON")

    override def read(value: JsValue): OkWrapper[T] = OkWrapper[T](
      fromField[Boolean](value, "ok"),
      fromField[T](value, "result")
    )
  }
}

object Protocol {
  object Responses {

    case class OkWrapper[T : JsonFormat](ok: Boolean, result: T)

    /**
      * This object represents an audio file to be treated as music by the Telegram clients.
      * @param file_id Unique identifier for this file
      * @param duration Duration of the audio in seconds as defined by sender
      * @param performer Performer of the audio as defined by sender or by audio tags
      * @param title Title of the audio as defined by sender or by audio tags
      * @param mime_type MIME type of the file as defined by sender
      * @param file_size File size
      */
    case class Audio(
      file_id: String,
      duration: Int,
      performer: Option[String],
      title: Option[String],
      mime_type: Option[String],
      file_size: Option[Int]
    )

    /**
      * This object represents an incoming callback query from a callback button in an inline keyboard. If the button
      * that originated the query was attached to a message sent by the bot, the field message will be presented.
      * If the button was attached to a message sent via the bot (in inline mode), the field inline_message_id
      * will be presented.
      * @param id Unique identifier for this query
      * @param from Sender
      * @param message Optional. Message with the callback button that originated the query. Note that message content and message date will not be available if the message is too old
      * @param inline_message_id Identifier of the message sent via the bot in inline mode, that originated the query
      * @param data Data associated with the callback button. Be aware that a bad client can send arbitrary data in this field
      */
    case class CallbackQuery(
      id: String,
      from: User,
      message: Option[Message],
      inline_message_id: Option[String],
      data: String
    )

    /**
      * This object represents a chat.
      * @param id Unique identifier for this chat. This number may be greater than 32 bits and some programming
      *           languages may have difficulty/silent defects in interpreting it. But it smaller than 52 bits,
      *           so a signed 64 bit integer or double-precision float type are safe for storing this identifier.
      * @param `type` Type of chat, can be either “private”, “group”, “supergroup” or “channel”
      * @param title Title, for channels and group chats
      * @param username Username, for private chats, supergroups and channels if available
      * @param first_name First name of the other party in a private chat
      * @param last_name Last name of the other party in a private chat
      */
    case class Chat(
      id: Long,
      `type`: String,
      title: Option[String],
      username: Option[String],
      first_name: Option[String],
      last_name: Option[String]
    )

    /**
      * Represents a result of an inline query that was chosen by the user and sent to their chat partner.
      * @param result_id The unique identifier for the result that was chosen
      * @param from The user that chose the result
      * @param location Sender location, only for bots that require user location
      * @param inline_message_id Identifier of the sent inline message. Available only if there is an inline keyboard
      *                          attached to the message. Will be also received in callback queries and can be used to
      *                          edit the message.
      * @param query The query that was used to obtain the result
      */
    case class ChosenInlineResult(
      result_id: String,
      from: User,
      location: Option[Location],
      inline_message_id: Option[String],
      query: String
    )

    /**
      * This object represents a phone contact.
      * @param phone_number Contact's phone number
      * @param first_name Contact's first name
      * @param last_name Contact's last name
      * @param user_id Contact's user identifier in Telegram
      */
    case class Contact(
      phone_number: String,
      first_name: String,
      last_name: Option[String],
      user_id: Option[Long]
    )

    /**
      * This object represents a general file (as opposed to photos, voice messages and audio files).
      * @param file_id Unique file identifier
      * @param thumb Document thumbnail as defined by sender
      * @param file_name Original filename as defined by sender
      * @param mime_type MIME type of the file as defined by sender
      * @param file_size File size
      */
    case class Document(
      file_id: String,
      thumb: Option[PhotoSize],
      file_name: Option[String],
      mime_type: Option[String],
      file_size: Option[Int]
    )

    /**
      * This object represents an incoming inline query. When the user sends an empty query, your bot could return
      * some default or trending results.
      * @param id Unique identifier for this query
      * @param from Sender
      * @param location Sender location, only for bots that request user location
      * @param query Text of the query (up to 512 characters)
      * @param offset Offset of the results to be returned, can be controlled by the bot
      */
    case class InlineQuery(
      id: String,
      from: User,
      location: Option[Location],
      query: String,
      offset: String
    )

    /**
      * This object represents a point on the map.
      * @param longitude Longitude as defined by sender
      * @param latitude Latitude as defined by sender
      */
    case class Location(
      longitude: Double,
      latitude: Double
    )

    /**
      * This object represents a message.
      * @param message_id Unique message identifier
      * @param from Sender, can be empty for messages sent to channels
      * @param date Date the message was sent in Unix time
      * @param chat Conversation the message belongs to
      * @param forward_from For forwarded messages, sender of the original message
      * @param forward_from_chat For messages forwarded from a channel, information about the original channel
      * @param forward_date For forwarded messages, date the original message was sent in Unix time
      * @param reply_to_message For replies, the original message. Note that the Message object in this field will not
      *                         contain further reply_to_message fields even if it itself is a reply.
      * @param edit_date Date the message was last edited in Unix time
      * @param text For text messages, the actual UTF-8 text of the message, 0-4096 characters.
      * @param entities For text messages, special entities like usernames, URLs, bot commands, etc. that appear
      *                 in the text
      * @param audio Message is an audio file, information about the file
      * @param document Message is a general file, information about the file
      * @param photo Message is a photo, available sizes of the photo
      * @param sticker Message is a sticker, information about the sticker
      * @param video Message is a video, information about the video
      * @param voice Message is a voice message, information about the file
      * @param caption Caption for the document, photo or video, 0-200 characters
      * @param contact Message is a shared contact, information about the contact
      * @param location Message is a shared location, information about the location
      * @param venue Message is a venue, information about the venue
      * @param new_chat_member A new member was added to the group, information about them (this member may be
      *                        the bot itself)
      * @param left_chat_member A member was removed from the group, information about them (this member may be the
      *                         bot itself)
      * @param new_chat_title A chat title was changed to this value
      * @param new_chat_photo A chat photo was change to this value
      * @param delete_chat_photo Service message: the chat photo was deleted
      * @param group_chat_created Service message: the group has been created
      * @param supergroup_chat_created Service message: the supergroup has been created. This field can‘t be received in
      *                                a message coming through updates, because bot can’t be a member of a supergroup
      *                                when it is created. It can only be found in reply_to_message if someone replies
      *                                to a very first message in a directly created supergroup.
      * @param channel_chat_created Service message: the channel has been created. This field can‘t be received in
      *                             a message coming through updates, because bot can’t be a member of a channel when
      *                             it is created. It can only be found in reply_to_message if someone replies to
      *                             a very first message in a channel.
      * @param migrate_to_chat_id The group has been migrated to a supergroup with the specified identifier. This number
      *                           may be greater than 32 bits and some programming languages may have difficulty/silent
      *                           defects in interpreting it. But it smaller than 52 bits, so a signed 64 bit integer or
      *                           double-precision float type are safe for storing this identifier.
      * @param migrate_from_chat_id The supergroup has been migrated from a group with the specified identifier.
      *                             This number may be greater than 32 bits and some programming languages may
      *                             have difficulty/silent defects in interpreting it. But it smaller than 52 bits, so
      *                             a signed 64 bit integer or double-precision float type are safe for storing
      *                             this identifier.
      * @param pinned_message Specified message was pinned. Note that the Message object in this field will not contain
      *                       further reply_to_message fields even if it is itself a reply.
      */
    case class Message(
      message_id: Long,
      from: Option[User],
      date: Long,
      chat: Chat,
      forward_from: Option[User],
      forward_from_chat: Option[Chat],
      forward_date: Option[Long],
      reply_to_message: Option[Message],
      edit_date: Option[Long],
      text: Option[String],
      entities: Option[Seq[MessageEntity]],
      audio: Option[Audio],
      document: Option[Document],
      photo: Option[Seq[PhotoSize]],
      sticker: Option[Sticker],
      video: Option[Video],
      voice: Option[Voice],
      caption: Option[String],
      contact: Option[Contact],
      location: Option[Location],
      venue: Option[Venue],
      new_chat_member: Option[User],
      left_chat_member: Option[User],
      new_chat_title: Option[String],
      new_chat_photo: Option[Seq[PhotoSize]],
      delete_chat_photo: Option[Boolean],
      group_chat_created: Option[Boolean],
      supergroup_chat_created: Option[Boolean],
      channel_chat_created: Option[Boolean],
      migrate_to_chat_id: Option[Long],
      migrate_from_chat_id: Option[Long],
      pinned_message: Option[Message]
    )

    /**
      * This object represents one special entity in a text message. For example, hashtags, usernames, URLs, etc.
      * @param `type` Type of the entity. Can be mention (@username), hashtag, bot_command, url, email,
      *               bold (bold text), italic (italic text), code (monowidth string), pre (monowidth block),
      *               text_link (for clickable text URLs), text_mention (for users without usernames)
      * @param offset Offset in UTF-16 code units to the start of the entity
      * @param length Length of the entity in UTF-16 code units
      * @param url For “text_link” only, url that will be opened after user taps on the text
      * @param user For “text_mention” only, the mentioned user
      */
    case class MessageEntity(
      `type`:String,
      offset: Int,
      length: Int,
      url: Option[String],
      user: Option[User]
    )

    /**
      * This object represents one size of a photo or a file / sticker thumbnail.
      * @param file_id Unique identifier for this file
      * @param width Photo width
      * @param height Photo height
      * @param file_size File size
      */
    case class PhotoSize(
      file_id: String,
      width: Int,
      height: Int,
      file_size: Option[Int]
    )

    /**
      * This object represents a sticker.
      * @param file_id Unique identifier for this file
      * @param width Sticker width
      * @param height Sticker height
      * @param thumb Sticker thumbnail in .webp or .jpg format
      * @param emoji Emoji associated with the sticker
      * @param file_size File size
      */
    case class Sticker(
      file_id: String,
      width: Int,
      height: Int,
      thumb: Option[PhotoSize],
      emoji: Option[String],
      file_size: Option[Int]
    )

    /**
      * This object represents an incoming update.
      * Only one of the optional parameters can be present in any given update.
      * @param update_id The update‘s unique identifier. Update identifiers start from a certain positive number and
      *                  increase sequentially. This ID becomes especially handy if you’re using Webhooks, since it
      *                  allows you to ignore repeated updates or to restore the correct update sequence, should they
      *                  get out of order.
      * @param message New incoming message of any kind — text, photo, sticker, etc.
      * @param edited_message New version of a message that is known to the bot and was edited
      * @param inline_query New incoming inline query
      * @param chosen_inline_result The result of an inline query that was chosen by a user
      *                             and sent to their chat partner.
      * @param callback_query New incoming callback query
      */
    case class Update(
      update_id: Long,
      message: Option[Message],
      edited_message: Option[Message],
      inline_query: Option[InlineQuery],
      chosen_inline_result: Option[ChosenInlineResult],
      callback_query: Option[CallbackQuery]
    )

    /**
      * This object represents a Telegram user or bot.
      * @param id Unique identifier for this user or bot
      * @param first_name User‘s or bot’s first name
      * @param last_name User‘s or bot’s last name
      * @param username User‘s or bot’s username
      */
    case class User(
      id: Long,
      first_name: String,
      last_name: Option[String],
      username: Option[String]
    )

    /**
      * This object represent a user's profile pictures.
      * @param total_count Total number of profile pictures the target user has
      * @param photos Requested profile pictures (in up to 4 sizes each)
      */
    case class UserProfilePhotos(
      total_count: Int,
      photos: Seq[PhotoSize]
    )

    /**
      * This object represents a venue.
      * @param location Venue location
      * @param title Name of the venue
      * @param address Address of the venue
      * @param foursquare_id Foursquare identifier of the venue
      */
    case class Venue(
      location: Location,
      title: String,
      address: String,
      foursquare_id: Option[String]
    )

    /**
      * This object represents a video file.
      * @param file_id Unique identifier for this file
      * @param width Video width as defined by sender
      * @param height Video height as defined by sender
      * @param duration Duration of the video in seconds as defined by sender
      * @param thumb Mime type of a file as defined by sender
      * @param mime_type Video thumbnail
      * @param file_size File size
      */
    case class Video(
      file_id: String,
      width: Int,
      height: Int,
      duration: Int,
      thumb: Option[PhotoSize],
      mime_type: Option[String],
      file_size: Option[Int]
    )

    /**
      * This object represents a voice note.
      * @param file_id Unique identifier for this file
      * @param duration Duration of the audio in seconds as defined by sender
      * @param mime_type MIME type of the file as defined by sender
      * @param file_size File size
      */
    case class Voice(
      file_id: String,
      duration: Int,
      mime_type: Option[String],
      file_size: Option[Int]
    )
  }
}
