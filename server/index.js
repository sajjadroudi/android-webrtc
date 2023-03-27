const http = require("http")
const Socket = require("websocket").server
const server = http.createServer(()=>{})

server.listen(3000,()=>{
    
})

const webSocket = new Socket({httpServer:server})

const users = []

webSocket.on('request',(req)=>{
    const connection = req.accept()
   

    connection.on('message',(message)=>{
        const receivedData = JSON.parse(message.utf8Data)

        console.log(receivedData);
        const user = findUser(receivedData.originUserName)
       
        switch(receivedData.type){
            case "REGISTER_USER":
                if(user != null){
                    //our user exists
                    connection.send(JSON.stringify({
                        type:'user already exists'
                    }))
                    return

                }

                const newUser = {
                    name: receivedData.originUserName, 
                    conn: connection
                }
                users.push(newUser)
            break

            case "START_CALL":
                const userToCall = findUser(receivedData.targetUserName)

                if(userToCall) {
                    connection.send(JSON.stringify({
                        type: "CALL_RESPONSE", 
                        isSuccessful: true
                    }))
                } else {
                    connection.send(JSON.stringify({
                        type: "CALL_RESPONSE", 
                        isSuccessful: false
                    }))
                }

            break
            
            case "SEND_OFFER":
                let userToReceiveOffer = findUser(receivedData.targetUserName)

                if (userToReceiveOffer){
                    userToReceiveOffer.conn.send(JSON.stringify({
                        type: "OFFER_RECEIVED",
                        originUserName: receivedData.originUserName,
                        data: receivedData.data.sdp
                    }))
                }
            break
                
            case "SEND_ANSWER":
                let userToReceiveAnswer = findUser(receivedData.targetUserName)
                if(userToReceiveAnswer){
                    userToReceiveAnswer.conn.send(JSON.stringify({
                        type: "ANSWER_RECEIVED",
                        originUserName: receivedData.originUserName,
                        data: receivedData.data.sdp
                    }))
                }
            break

            case "ICE_CANDIDATE":
                let userToReceiveIceCandidate = findUser(receivedData.targetUserName)
                if(userToReceiveIceCandidate){
                    userToReceiveIceCandidate.conn.send(JSON.stringify({
                        type:"ICE_CANDIDATE",
                        originUserName: receivedData.originUserName,
                        data:{
                            sdpMLineIndex: receivedData.data.sdpMLineIndex,
                            sdpMid: receivedData.data.sdpMid,
                            sdpCandidate: receivedData.data.sdpCandidate
                        }
                    }))
                }
            break

            case "REJECT":
                let rejectedUser = findUser(receivedData.targetUserName)

                if (rejectedUser){
                    rejectedUser.conn.send(JSON.stringify({
                        type:"CALL_REJECTED"
                    }))
                }

            break

            case "END_CALL":
                const targetUser = findUser(receivedData.targetUserName)
                
                if(targetUser) {
                    targetUser.conn.send(JSON.stringify({
                        type: "CALL_ENDED"
                    }))
                }

            break;

            case "UNREGISTER_USER":
                // TODO
            break;

        }

    })
    
    connection.on('close', () =>{
        users.forEach( user => {
            if(user.conn === connection){
                users.splice(users.indexOf(user),1)
            }
        })
    })

})

const findUser = username =>{
    for(let i=0; i<users.length;i++){
        if(users[i].name === username)
        return users[i]
    }
}