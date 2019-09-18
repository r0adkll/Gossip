import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

let db = admin.firestore();
let defaultMessaging = admin.messaging();

exports.sendMessage = functions.firestore
    .document('threads/general/messages/{messageId}')
    .onWrite(((change, context) => {
        // let messageId = context.params.messageId;
        let messageText = change.after.get('value');
        let messageUserId = change.after.get('user.id');
        let messageUser = change.after.get('user.name');

        // Find all push tokens
        let query = db.collection('users');

        return query.get()
            .then(querySnapshot => {
                let tokens: {[field: string]: any}[] = [];
                querySnapshot.forEach(snapshot => {
                    let userId = snapshot.get("user.id");
                    let token = snapshot.get("pushToken") as string;
                    if (token != null && token.length != 0 && userId != messageUserId) {
                        tokens.push({
                            userId: userId,
                            token: token
                        });
                    }
                });

                // Now fetch the last 3 messages
                let latestMessagesQuery = db.collection('threads/general/messages');
                return latestMessagesQuery
                    .orderBy('createdAt', "desc")
                    .limit(3)
                    .get()
                    .then(latestQuerySnapshot => {
                        let messages: {[field: string]: any}[] = [];
                        latestQuerySnapshot.forEach(snapshot => {
                            messages.push(snapshot.data())
                        });

                        const payload = {
                            notification: {
                                title: messageText,
                                body: 'Sent from ' + messageUser
                            },
                            data: {
                                "lastMessages": JSON.stringify(messages)
                            }
                        };

                        return defaultMessaging.sendToDevice(tokens.map(user => user.token), payload);
                    });
            });
    }));