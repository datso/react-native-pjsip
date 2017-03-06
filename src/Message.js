/**
 * This class describes the information and current status of a call.
 */
export default class Message {

    constructor({
            accountId,
            contactUri,
            fromUri,
            toUri,
            body,
            contentType
        }) {
        let fromNumber = null;
        let fromName = null;

        if (fromUri) {
            let match = fromUri.match(/"([^"]+)" <sip:([^@]+)@/);

            if (match) {
                fromName = match[1];
                fromNumber = match[2];
            } else {
                match = fromUri.match(/sip:([^@]+)@/);

                if (match) {
                    fromNumber = match[1];
                }
            }
        }

        this._accountId = accountId;
        this._contactUri = contactUri;
        this._fromUri = fromUri;
        this._fromName = fromName;
        this._fromNumber = fromNumber;
        this._toUri = toUri;
        this._body = body;
        this._contentType = contentType;
    }

    /**
     * The account ID where this message belongs.
     * @returns {int}
     */
    getAccountId() {
        return this._accountId;
    }

    /**
     * The Contact URI of the sender, if present.
     * @returns {String}
     */
    getContactUri() {
        return this._contactUri;
    }

    /**
     * URI of the sender.
     * @returns {String}
     */
    getFromUri() {
        return this._fromUri;
    }

    /**
     * Sender name, or NULL if no name specified in URI.
     * @returns {String}
     */
    getFromName() {
        return this._fromName;
    }

    /**
     * Sender number
     * @returns {String}
     */
    getFromNumber() {
        return this._fromNumber;
    }

    /**
     * URI of the destination message.
     * @returns {String}
     */
    getToUri() {
        return this._toUri;
    }

    /**
     * Message body, or NULL if no message body is attached to this mesage.
     * @returns {String}
     */
    getBody() {
        return this._body;
    }

    /**
     * MIME type of the message.
     * @returns {String}
     */
    getContentType() {
        return this._contentType;
    }

}