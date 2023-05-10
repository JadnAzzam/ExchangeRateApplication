from ..app import db, ma

class PeerToPeerTransactionRequest(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    usd_amount = db.Column(db.Float)
    lbp_amount = db.Column(db.Float)
    usd_to_lbp = db.Column(db.Boolean)  # according to the SENDER

    sender_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)
    receiver_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)

    # pending, accepted, rejected, complete(u can delete instead of marking complete
    exchange_stage = db.Column(db.String(16))

    def __init__(self, usd_amount, lbp_amount, usd_to_lbp, sender_id, receiver_id):
        super(PeerToPeerTransactionRequest, self).__init__(
            usd_amount=usd_amount,
            lbp_amount=lbp_amount,
            usd_to_lbp=usd_to_lbp,
            sender_id=sender_id,
            receiver_id=receiver_id,
            exchange_stage="pending"
        )


class PeerToPeerTransactionRequestSchema(ma.Schema):
    class Meta:
        fields = ("id", "usd_amount", "lbp_amount", "usd_to_lbp", "sender_id", "receiver_id")  # "exchange_stage")
        model = PeerToPeerTransactionRequest
