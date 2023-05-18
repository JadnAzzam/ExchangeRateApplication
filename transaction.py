from ..app import db, ma
import datetime


class Transaction(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    usd_amount = db.Column(db.Float)
    lbp_amount = db.Column(db.Float)
    usd_to_lbp = db.Column(db.Boolean)

    added_date = db.Column(db.DateTime)
    sender_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)
    receiver_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)

    def __init__(self, usd_amount, lbp_amount, usd_to_lbp, sender_id, receiver_id):
        super(Transaction, self).__init__(
            usd_amount=usd_amount,
            lbp_amount=lbp_amount,
            usd_to_lbp=usd_to_lbp,
            sender_id=sender_id,
            receiver_id=receiver_id,
            added_date=datetime.datetime.now()
        )

    def __repr__(self):
        s = ""
        if self.usd_to_lbp:
            s = "sold "
        else:
            s = "bought "
        s += str(self.usd_amount) + " $ for " + str(self.lbp_amount) + " LBP\n"
        return s


class TransactionSchema(ma.Schema):
    class Meta:
        fields = ("id", "usd_amount", "lbp_amount", "usd_to_lbp", "added_date", "sender_id", "receiver_id")
        model = Transaction

