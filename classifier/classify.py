import time

from flask import Flask, request
from flask_cors import CORS
from utils import *

##################################################
# API part
##################################################
app = Flask(__name__)
cors = CORS(app)


@app.route("/api/predict", methods=['POST'])
def predict():
    max_post_text_len = 39
    image_features = [[]]

    start = time.time()

    data = request.get_json()

    post_texts = []
    post_text_lens = []
    target_descriptions = []
    target_description_lens = []

    each_post_text = " ".join(data["postText"])
    each_target_description = data["targetTitle"]
    if word2id:
        if (each_post_text + " ").isspace():
            post_texts.append([0])
            post_text_lens.append(1)
        else:
            each_post_tokens = tokenise(each_post_text)
            post_texts.append([word2id.get(each_token, 1) for each_token in each_post_tokens])
            post_text_lens.append(len(each_post_tokens))
    else:
        post_texts.append([each_post_text])
    if False:  # use_target_description
        if word2id:
            if (each_target_description + " ").isspace():
                target_descriptions.append([0])
                target_description_lens.append(1)
            else:
                each_target_description_tokens = tokenise(each_target_description)
                target_descriptions.append(
                    [word2id.get(each_token, 1) for each_token in each_target_description_tokens])
                target_description_lens.append(len(each_target_description_tokens))
        else:
            target_descriptions.append([each_target_description])
    else:
        target_descriptions.append([])
        target_description_lens.append(0)

    post_texts = np.array(post_texts)
    post_text_lens = [each_len if each_len <= max_post_text_len else max_post_text_len for each_len in  # max_post_text_len
                      post_text_lens]
    post_text_lens = np.array(post_text_lens)
    post_texts = pad_sequences(post_texts, max_post_text_len)  # max_post_text_len

    if not False:  # use_target_description
        max_post_text_len = 0
    target_descriptions = np.array(target_descriptions)
    target_description_lens = [
        each_len if each_len <= 0 else 0 for each_len in  # max_target_description_len
        target_description_lens]
    target_description_lens = np.array(target_description_lens)
    target_descriptions = pad_sequences(target_descriptions, 0)  # max_target_description_len

    all_prediction = []
    all_distribution = []

    for i in range(1, 6):
        tf.reset_default_graph()
        saver = tf.train.import_meta_graph(os.path.join("data", "runs", "0715", "checkpoints", "SAN"+str(i)+".meta"), clear_devices=True)
        with tf.Session() as sess:
            saver.restore(sess, os.path.join("data", "runs", "0715", "checkpoints", "SAN"+str(i)))
            g = tf.get_default_graph()
            input_x1 = g.get_tensor_by_name("post_text:0")
            input_x1_len = g.get_tensor_by_name("post_text_len:0")
            dropout_rate_hidden = g.get_tensor_by_name("dropout_rate_hidden:0")
            dropout_rate_cell = g.get_tensor_by_name("dropout_rate_cell:0")
            dropout_rate_embedding = g.get_tensor_by_name("dropout_rate_embedding:0")
            batch_size = g.get_tensor_by_name("batch_size:0")
            input_x2 = g.get_tensor_by_name("target_description:0")
            input_x2_len = g.get_tensor_by_name("target_description_len:0")
            input_x3 = g.get_tensor_by_name("image_feature:0")
            output_prediction = g.get_tensor_by_name("prediction:0")
            output_distribution = g.get_tensor_by_name("distribution:0")
            feed_dict = {input_x1: post_texts,
                         input_x1_len: post_text_lens,
                         dropout_rate_hidden: 0,
                         dropout_rate_cell: 0,
                         dropout_rate_embedding: 0,
                         batch_size: len(post_texts),
                         input_x2: target_descriptions,
                         input_x2_len: target_description_lens,
                         input_x3: image_features}
            prediction, distribution = sess.run([output_prediction, output_distribution], feed_dict)
            prediction = np.ravel(prediction).astype(np.float32)
            all_prediction.append(prediction)
            all_distribution.append(distribution)
            if False:  # if_annotated
                print mse(prediction, truth_means)
                print acc(distribution2label(truth_classes), distribution2label(distribution))

    avg_prediction = np.mean(all_prediction, axis=0)
    avg_distribution = np.mean(all_distribution, axis=0)
    print all_prediction
    result = json.dumps({"id": data["id"], "clickbaitScore": float(avg_prediction[0])}) + '\n'

    ##################################################

    print("Time spent handling the request: %f" % (time.time() - start))

    return result


if __name__ == "__main__":
    with open(os.path.join('data', 'word2id.json'), 'r') as fin:
        word2id = json.load(fin)

    app.run()
