import datetime
import statistics
import calendar
import jwt

from flask import Flask, request, jsonify, abort
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_marshmallow import Marshmallow
from flask_bcrypt import Bcrypt

from . import db_config


app = Flask(__name__)
app.config[
    'SQLALCHEMY_DATABASE_URI'] = db_config.DB_CONFIG
CORS(app)
db = SQLAlchemy(app)
ma = Marshmallow(app)
bcrypt = Bcrypt(app)

from .model.user import UserSchema, User
from .model.transaction import Transaction, TransactionSchema
from .model.PeerToPeerTransaction import PeerToPeerTransactionRequestSchema, PeerToPeerTransactionRequest

SECRET_KEY = "b'|\xe7\xbfU3`\xc4\xec\xa7\xa9zf:}\xb5\xc7\xb9\x139^3@Dv'"

transaction_schema = TransactionSchema()
transactions_schema = TransactionSchema(many=True)

user_schema = UserSchema()


p2pTransaction_schema = PeerToPeerTransactionRequestSchema()
p2pTransactions_schema = PeerToPeerTransactionRequestSchema(many=True)




def create_token(user_id):
    payload = {
        'exp': datetime.datetime.utcnow() + datetime.timedelta(days=4),
        'iat': datetime.datetime.utcnow(),
        'sub': user_id
    }
    return jwt.encode(payload, SECRET_KEY, algorithm='HS256')


@app.route('/transaction', methods=['POST'])
def handle_transaction():
    transactionContent = request.get_json(force=True)
    Tusd_amount = transactionContent["usd_amount"]
    Tlbp_amount = transactionContent["lbp_amount"]
    Tusd_to_lbp = transactionContent["usd_to_lbp"]
    Tsender_id = None
    Treceiver_id = None
    token = extract_auth_token(request)
    print("token: ", token)
    if token != "null":
        try:
            extractedID = decode_token(token)
            Tsender_id = extractedID

        except jwt.ExpiredSignatureError:
            abort(403)
        except jwt.InvalidTokenError:
            abort(403)

    T = Transaction(Tusd_amount, Tlbp_amount, Tusd_to_lbp, Tsender_id, Treceiver_id)
    db.session.add(T)
    db.session.commit()
    return jsonify(transaction_schema.dump(T))

@app.route('/exchangeRate', methods=['GET'])
def get_exchange_rate():
    avg_usd_to_lbp = 0
    avg_lbp_to_usd = 0
    usd_to_lbp_transactions = Transaction.query.filter(Transaction.added_date.between(
        datetime.datetime.now() - datetime.timedelta(3), datetime.datetime.now()), Transaction.usd_to_lbp == True).all()
    lbp_to_usd_transactions = Transaction.query.filter(Transaction.added_date.between(
        datetime.datetime.now() - datetime.timedelta(3), datetime.datetime.now()),
        Transaction.usd_to_lbp == False).all()
    if len(usd_to_lbp_transactions) != 0:
        for i in range(len(usd_to_lbp_transactions)):
            usd_amount_entry = usd_to_lbp_transactions[i].usd_amount
            lbp_amount_entry = usd_to_lbp_transactions[i].lbp_amount
            rate = lbp_amount_entry / usd_amount_entry
            avg_usd_to_lbp = avg_usd_to_lbp + rate
        avg_usd_to_lbp = avg_usd_to_lbp / len(usd_to_lbp_transactions)
    if len(lbp_to_usd_transactions) != 0:
        for i in range(len(lbp_to_usd_transactions)):
            usd_amount_entry = lbp_to_usd_transactions[i].usd_amount
            lbp_amount_entry = lbp_to_usd_transactions[i].lbp_amount
            rate = lbp_amount_entry / usd_amount_entry
            avg_lbp_to_usd = avg_lbp_to_usd + rate
        avg_lbp_to_usd = avg_lbp_to_usd / len(lbp_to_usd_transactions)
    result = {"usd_to_lbp": round(avg_usd_to_lbp, 2), "lbp_to_usd": round(avg_lbp_to_usd, 2)}
    return jsonify(result)


@app.route('/user', methods=['POST'])
def new_user():
    if request.method == 'POST':
        user_name = request.json["user_name"]
        password = request.json["password"]
        newUser = User(user_name, password)
        db.session.add(newUser)
        db.session.commit()
    return jsonify(user_schema.dump(newUser))


@app.route('/authentication', methods=['POST'])
def authenticate():
    if request.method == 'POST':
        user_name = request.json["user_name"]
        password = request.json["password"]
        if (user_name is None) or (password is None):
            abort(400)
        user = User.query.filter_by(user_name=user_name).first()
        if user is None:
            abort(403)
        checkPW = bcrypt.check_password_hash(user.hashed_password, password)
        if not checkPW:
            abort(403)
        token = create_token(user.id)

    return jsonify({"token": token})


def extract_auth_token(authenticated_request):
    auth_header = authenticated_request.headers.get('Authorization')
    if auth_header:
        return auth_header.split(" ")[1]
    else:
        return None


def decode_token(token):
    payload = jwt.decode(token, SECRET_KEY, 'HS256')
    return payload['sub']


@app.route('/transaction', methods=['GET'])
def fetch_transaction():
    if request.method == 'GET':
        user_token = extract_auth_token(request)
        if user_token is None:
            abort(403)
        else:
            try:
                userId = decode_token(user_token)
                previous_transactions = Transaction.query.filter_by(sender_id=userId).all()
            except (jwt.ExpiredSignatureError, jwt.InvalidTokenError):
                abort(403)
    return jsonify(transactions_schema.dump(previous_transactions))

@app.route('/allUserIDs', methods=['GET'])
def get_all_user_IDs():
    allUsers = User.query.all()
    IDs = []
    for i in range(len(allUsers)):
        IDs.append(allUsers[i].id)

    return jsonify(IDs)


@app.route('/userBalance', methods=['GET'])
def get_user_balance():
    token = extract_auth_token(request)
    if token is None:
        abort(403)
    try:
        extractedID = decode_token(token)
    except jwt.ExpiredSignatureError:
        abort(403)
    except jwt.InvalidTokenError:
        abort(403)

    userBalance_USD = User.query.filter_by(id=extractedID).first().usd_amount_owned
    userBalance_LBP = User.query.filter_by(id=extractedID).first().lbp_amount_owned
    D = {
        "user_usd_amount_owned": userBalance_USD,
        "user_lbp_amount_owned": userBalance_LBP
    }
    return jsonify(D)


def get_rates_between_dates(START_DATE, END_DATE):
    usd_to_lbp_entries = Transaction.query.filter(
        Transaction.added_date.between(START_DATE, END_DATE), Transaction.usd_to_lbp == True).all()
    lbp_to_usd_entries = Transaction.query.filter(
        Transaction.added_date.between(START_DATE, END_DATE), Transaction.usd_to_lbp == False).all()
    sell_usd_rates = []
    buy_usd_rates = []

    for i in range(len(usd_to_lbp_entries)):
        sell_usd_rates.append(usd_to_lbp_entries[i].lbp_amount / usd_to_lbp_entries[i].usd_amount)
    for i in range(len(lbp_to_usd_entries)):
        buy_usd_rates.append(lbp_to_usd_entries[i].lbp_amount / lbp_to_usd_entries[i].usd_amount)

    if len(sell_usd_rates) == 0:
        usd_to_lbp_rate = 0
    else:
        usd_to_lbp_rate = round((sum(sell_usd_rates) / len(sell_usd_rates)), 2)

    if len(buy_usd_rates) == 0:
        lbp_to_usd_rate = 0
    else:
        lbp_to_usd_rate = round((sum(buy_usd_rates) / len(buy_usd_rates)), 2)

    D = {
        "usd_to_lbp": usd_to_lbp_rate,
        "lbp_to_usd": lbp_to_usd_rate
    }
    return D


@app.route('/exchangeRateOverTime', methods=['GET'])
def get_exchange_rate_over_time():
    sell_usd_ratesOverTime = []
    sell_usd_ratesOverTime_dates = []
    buy_usd_ratesOverTime = []
    buy_usd_ratesOverTime_dates = []

    current_year = datetime.date.today().year
    delta = datetime.timedelta(days=31)

    for month in range(1, 13):
        start_date = datetime.datetime(current_year, month, 1)
        end_date = start_date + delta
        exchange_rate_usd = get_rates_between_dates(start_date, end_date)['usd_to_lbp']
        exchange_rate_lbp = get_rates_between_dates(start_date, end_date)['lbp_to_usd']
        sell_usd_ratesOverTime.append(exchange_rate_usd)
        buy_usd_ratesOverTime.append(exchange_rate_lbp)
        month_abbr = calendar.month_abbr[month]
        sell_usd_ratesOverTime_dates.append(month_abbr)
        buy_usd_ratesOverTime_dates.append(month_abbr)
        end_date += delta

    min_sell = -1
    for rate in sell_usd_ratesOverTime:
        if rate > 0:
            if min_sell == -1 or min_sell > rate:
                min_sell = rate

    min_buy = -1
    for rate in buy_usd_ratesOverTime:
        if rate > 0:
            if min_buy == -1 or min_buy > rate:
                min_buy = rate

    D = {
        "usd_to_lbp_ratesOverTime": sell_usd_ratesOverTime,
        "usd_to_lbp_ratesOverTime_dates": sell_usd_ratesOverTime_dates,
        "lbp_to_usd_ratesOverTime": buy_usd_ratesOverTime,
        "lbp_to_usd_ratesOverTime_dates": buy_usd_ratesOverTime_dates,
        "sell_usd_max_rate": max(sell_usd_ratesOverTime),
        "buy_usd_max_rate": max(buy_usd_ratesOverTime),
        "sell_usd_min_rate": min_sell,
        "buy_usd_min_rate": min_buy,
        "num_of_sell_usd_transactions": Transaction.query.filter(Transaction.usd_to_lbp == True).count(),
        "num_of_buy_usd_transactions": Transaction.query.filter(Transaction.usd_to_lbp == False).count(),
        "std_dev_sell_usd": statistics.stdev(sell_usd_ratesOverTime),
        "std_dev_buy_usd": statistics.stdev(buy_usd_ratesOverTime)
    }
    return D



@app.route('/postTransactionRequest', methods=['POST'])
def post_transaction_request():  # send a trade offer to someone
    requestContent = request.get_json(force=True)

    sender_id = None
    receiver_id = requestContent["receiver_id"]
    usd_amount = requestContent["usd_amount"]
    lbp_amount = requestContent["lbp_amount"]
    usd_to_lbp = requestContent["usd_to_lbp"]  # bool

    token = extract_auth_token(request)
    if token is None:
        abort(403)
    try:
        sender_id = decode_token(token)
    except jwt.ExpiredSignatureError:
        abort(403)
    except jwt.InvalidTokenError:
        abort(403)

    if sender_id == receiver_id or sender_id is None or receiver_id is None:
        abort(400)

    if usd_to_lbp == "usd-to-lbp":
        usd_to_lbp = True
    else:
        usd_to_lbp = False

    X = PeerToPeerTransactionRequest(usd_amount, lbp_amount, usd_to_lbp, sender_id, receiver_id)

    db.session.add(X)
    db.session.commit()

    return jsonify(p2pTransaction_schema.dump(X))


@app.route('/fetchTransactionRequests', methods=['GET'])
def fetch_transaction_requests():
    token = extract_auth_token(request)
    if token is None:
        abort(403)
    try:
        extractedID = decode_token(token)
    except jwt.ExpiredSignatureError:
        abort(403)
    except jwt.InvalidTokenError:
        abort(403)

    pendingSentRequests = PeerToPeerTransactionRequest.query.filter_by(sender_id=extractedID, exchange_stage="pending")
    pendingReceivedRequests = PeerToPeerTransactionRequest.query.filter_by(receiver_id=extractedID,
                                                                           exchange_stage="pending")
    acceptedSentRequests = PeerToPeerTransactionRequest.query.filter_by(sender_id=extractedID,
                                                                        exchange_stage="accepted")
    rejectedSentRequests = PeerToPeerTransactionRequest.query.filter_by(sender_id=extractedID,
                                                                        exchange_stage="rejected")

    D = {
        "pendingSentRequests": p2pTransactions_schema.dump(pendingSentRequests.all()),
        "pendingReceivedRequests": p2pTransactions_schema.dump(pendingReceivedRequests.all()),
        "acceptedSentRequests": p2pTransactions_schema.dump(acceptedSentRequests.all()),
        "rejectedSentRequests": p2pTransactions_schema.dump(rejectedSentRequests.all())
    }

    acceptedSentRequests.delete()
    rejectedSentRequests.delete()
    db.session.commit()

    return jsonify(D)


@app.route('/respondToTransactionRequest', methods=['POST'])
def respond_to_transaction_request():
    transactionResponseContent = request.get_json(force=True)
    transactionAccepted = transactionResponseContent["accepted"]
    transactionID = transactionResponseContent["transaction_id"]
    transaction = PeerToPeerTransactionRequest.query.filter_by(id=transactionID).first()

    if transactionAccepted == "rejected":
        addTransaction = False
        transaction.exchange_stage = "rejected"
        mul = 0
    elif transactionAccepted == "accepted":
        addTransaction = True
        transaction.exchange_stage = "accepted"
        mul = 1
        if not transaction.usd_to_lbp:
            mul = -1
    else:
        abort(400)

    sender = User.query.filter_by(id=transaction.sender_id).first()
    receiver = User.query.filter_by(id=transaction.receiver_id).first()

    sender.usd_amount_owned += (-mul) * transaction.usd_amount
    sender.lbp_amount_owned += mul * transaction.lbp_amount
    receiver.usd_amount_owned += mul * transaction.usd_amount
    receiver.lbp_amount_owned += (-mul) * transaction.lbp_amount

    if (sender.usd_amount_owned < 0 or
            sender.lbp_amount_owned < 0 or
            receiver.usd_amount_owned < 0 or
            receiver.lbp_amount_owned < 0):
        abort(400)

    if addTransaction:
        T = Transaction(transaction.usd_amount, transaction.lbp_amount, transaction.usd_to_lbp, sender.id, receiver.id)
        db.session.add(T)

    db.session.commit()

    D = {
        "sender_new_usd_owned": sender.usd_amount_owned,
        "sender_new_lbp_owned": sender.lbp_amount_owned,
        "receiver_new_usd_owned": receiver.usd_amount_owned,
        "receiver_new_lbp_owned": receiver.lbp_amount_owned
    }
    return jsonify(D)

