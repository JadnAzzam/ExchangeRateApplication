from ..app import db, ma, bcrypt


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_name = db.Column(db.String(30), unique=True)
    hashed_password = db.Column(db.String(128))
    usd_amount_owned = db.Column(db.Float)
    lbp_amount_owned = db.Column(db.Float)

    def __init__(self, user_name, password):
        super(User, self).__init__(user_name=user_name)
        self.hashed_password = bcrypt.generate_password_hash(password)
        self.lbp_amount_owned = 100000
        self.usd_amount_owned = 100


class UserSchema(ma.Schema):
    class Meta:
        fields = ("id", "user_name", "lbp_amount_owned", "usd_amount_owned")
        model = User


