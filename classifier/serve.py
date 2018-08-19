import datetime
import time
import train as train
from threading import Thread
import tensorflow as tf
from flask import Flask, request
from flask import jsonify
from flask_cors import CORS
from freeze import freeze_graph
from utils import *
import shutil
import warnings

warnings.filterwarnings("ignore", message="numpy.dtype size changed")

app = Flask(__name__)
cors = CORS(app)


@app.route("/predict", methods=['POST'])
def predict():
    start = time.time()
    max_post_text_len = 39
    data = request.get_json()

    post_texts = []
    post_text_lens = []
    target_descriptions = []
    target_description_lens = []

    each_post_text = " ".join(data["postText"])
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

    target_descriptions.append([])
    target_description_lens.append(0)

    post_texts = np.array(post_texts)
    post_text_lens = [each_len if each_len <= max_post_text_len else max_post_text_len for each_len in
                      # max_post_text_len
                      post_text_lens]
    post_text_lens = np.array(post_text_lens)
    post_texts = pad_sequences(post_texts, max_post_text_len)  # max_post_text_len

    feed_dict_SAN1 = {input_x1_SAN1: post_texts,
                      input_x1_len_SAN1: post_text_lens,
                      dropout_rate_cell_SAN1: 0,
                      batch_size_SAN1: len(post_texts)
                      }

    feed_dict_SAN2 = {input_x1_SAN2: post_texts,
                      input_x1_len_SAN2: post_text_lens,
                      dropout_rate_cell_SAN2: 0,
                      batch_size_SAN2: len(post_texts)
                      }
    feed_dict_SAN3 = {input_x1_SAN3: post_texts,
                      input_x1_len_SAN3: post_text_lens,
                      dropout_rate_cell_SAN3: 0,
                      batch_size_SAN3: len(post_texts)
                      }
    feed_dict_SAN4 = {input_x1_SAN4: post_texts,
                      input_x1_len_SAN4: post_text_lens,
                      dropout_rate_cell_SAN4: 0,
                      batch_size_SAN4: len(post_texts)
                      }
    feed_dict_SAN5 = {input_x1_SAN5: post_texts,
                      input_x1_len_SAN5: post_text_lens,
                      dropout_rate_cell_SAN5: 0,
                      batch_size_SAN5: len(post_texts)
                      }

    prediction_SAN1, distribution_SAN1 = sess_SAN1.run([output_prediction_SAN1, output_distribution_SAN1],
                                                       feed_dict_SAN1)
    prediction_SAN2, distribution_SAN2 = sess_SAN2.run([output_prediction_SAN2, output_distribution_SAN2],
                                                       feed_dict_SAN2)
    prediction_SAN3, distribution_SAN3 = sess_SAN3.run([output_prediction_SAN3, output_distribution_SAN3],
                                                       feed_dict_SAN3)
    prediction_SAN4, distribution_SAN4 = sess_SAN4.run([output_prediction_SAN4, output_distribution_SAN4],
                                                       feed_dict_SAN4)
    prediction_SAN5, distribution_SAN5 = sess_SAN5.run([output_prediction_SAN5, output_distribution_SAN5],
                                                       feed_dict_SAN5)

    prediction_mean = np.mean([float(prediction_SAN1[0][0]), float(prediction_SAN2[0][0]), float(prediction_SAN3[0][0]),
                               float(prediction_SAN4[0][0]), float(prediction_SAN5[0][0])])

    result = json.dumps({"id": data["id"], "clickbaitScore": prediction_mean})
    print("Time spent handling the request: %f" % (time.time() - start))

    return result


@app.route("/train", methods=['POST'])
def retrain():
    print "retrain request received"
    data = request.get_json()

    now = datetime.datetime.now()
    new_timestamp = '{:02d}'.format(now.day) + '{:02d}'.format(now.month) + '{:02d}'.format(now.year)
    print "new timestamp: " + new_timestamp

    def retrain_model(data, new_timestamp):
        global timestamp

        # generate embeddings
        print "load previous word2vec model"
        model = Word2Vec.load("data/" + timestamp + "/s_clickbait.100.model")

        sentences = []
        for each_line in data['instances']:
            each_item = each_line
            for each_sentence in each_item["postText"]:
                sentences.append(tokenise(each_sentence))
            if each_item["targetTitle"]:
                sentences.append(tokenise(each_item["targetTitle"]))
            if each_item["targetDescription"]:
                sentences.append(tokenise(each_item["targetDescription"]))
            for each_sentence in each_item["targetParagraphs"]:
                sentences.append(tokenise(each_sentence))
            for each_sentence in each_item["targetCaptions"]:
                sentences.append(tokenise(each_sentence))
        print "sentences read and tokenised"

        model.build_vocab(sentences, update=True)
        print "vocabulary updated"
        model.train(sentences, total_examples=len(sentences), epochs=model.epochs)
        print "retrained model"

        if os.path.exists("data/" + new_timestamp):
            shutil.rmtree("data/" + new_timestamp)
            print "removed files from directory"

        os.makedirs("data/" + new_timestamp)
        print "make dir: data/" + new_timestamp

        model.save("data/" + new_timestamp + "/s_clickbait.100.model")
        print "saved word2vec model"
        model.wv.save_word2vec_format("data/" + new_timestamp + "/s_clickbait.100.txt", binary=False)
        print "saved word2vec model in C format"

        # write data into train data csv
        ids = {}
        for each_line in data['instances']:
            ids[each_line['id']] = False

        with open('data/clickbait17-train-170331/instances.jsonl', 'ab+') as fin:
            for each_line in fin:
                each_item = json.loads(each_line.decode('utf-8'))
                instance_id = each_item['id']
                if instance_id in ids.keys():
                    ids[instance_id] = True

            for each_line in data['instances']:
                if not ids[each_line['id']]:
                    fin.write('\n' + json.dumps(each_line))

            fin.close()

        print "wrote new data into train instances"

        with open('data/clickbait17-train-170331/truth.jsonl', 'r+') as fin:
            truth_lines = {}
            for each_line in fin:
                each_item = json.loads(each_line.decode('utf-8'))
                truth_lines[each_item['id']] = each_item

            fin.seek(0)

            for each_line in data['truth']:
                truth_id = each_line['id']
                if truth_id in truth_lines.keys():
                    truth_lines[truth_id]['truthJudgments'] = each_line['truthJudgments'] + truth_lines[truth_id][
                        'truthJudgments']
                    truth_lines[truth_id]['truthMean'] = np.mean(truth_lines[truth_id]['truthJudgments'])
                    truth_lines[truth_id]['truthMode'] = max(set(truth_lines[truth_id]['truthJudgments']),
                                                             key=truth_lines[truth_id]['truthJudgments'].count)

                    # set truthClass
                    mean = truth_lines[truth_id]['truthMean']
                    if mean < 0.5:
                        truth_lines[truth_id]['truthClass'] = "no-clickbait"
                    else:
                        truth_lines[truth_id]['truthClass'] = "clickbait"
                else:
                    truth_lines[truth_id] = each_line

            for i, truth in enumerate(truth_lines.values()):
                if i == len(truth_lines.values()) - 1:
                    fin.write(json.dumps(truth))
                else:
                    fin.write(json.dumps(truth) + '\n')

            fin.truncate()
            fin.close()

        print "truth data updated"

        # retrain model
        print "start training models"
        train.main('data/' + new_timestamp)
        print "models trained (checkpoints)"

        if not os.path.exists("data/" + new_timestamp + "/runs/models"):
            os.makedirs("data/" + new_timestamp + "/runs/models")
            print "make dir: data/" + new_timestamp + "/runs/models"

        # convert checkpoints to models
        freeze_graph("data/" + new_timestamp + "/runs", "prediction,distribution")
        print "freeze models"

        # reload models for serving
        load_model(new_timestamp)
        print "loaded new models"

        # set global timestamp
        timestamp = new_timestamp
        print "set new timestamp: " + new_timestamp

    thread = Thread(target=retrain_model, kwargs={'data': data, 'new_timestamp': new_timestamp})
    thread.start()

    return jsonify({'message': 'Re-train started'})


def load_graph(frozen_graph_filename):
    # We load the protobuf file from the disk and parse it to retrieve the 
    # unserialized graph_def
    with tf.gfile.GFile(frozen_graph_filename, "rb") as f:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())

    # Then, we import the graph_def into a new Graph and returns it 
    with tf.Graph().as_default() as graph:
        # The name var will prefix every op/nodes in your graph
        # Since we load everything in a new graph, this is not needed
        tf.import_graph_def(graph_def, name="clickbait")
    return graph


def load_model(target_timestamp):
    global word2id
    global graph_SAN1, input_x1_SAN1, input_x1_len_SAN1, dropout_rate_cell_SAN1, batch_size_SAN1, output_prediction_SAN1, output_distribution_SAN1, sess_SAN1
    global graph_SAN2, input_x1_SAN2, input_x1_len_SAN2, dropout_rate_cell_SAN2, batch_size_SAN2, output_prediction_SAN2, output_distribution_SAN2, sess_SAN2
    global graph_SAN3, input_x1_SAN3, input_x1_len_SAN3, dropout_rate_cell_SAN3, batch_size_SAN3, output_prediction_SAN3, output_distribution_SAN3, sess_SAN3
    global graph_SAN4, input_x1_SAN4, input_x1_len_SAN4, dropout_rate_cell_SAN4, batch_size_SAN4, output_prediction_SAN4, output_distribution_SAN4, sess_SAN4
    global graph_SAN5, input_x1_SAN5, input_x1_len_SAN5, dropout_rate_cell_SAN5, batch_size_SAN5, output_prediction_SAN5, output_distribution_SAN5, sess_SAN5

    with open(os.path.join('data', target_timestamp, 'word2id.json'), 'r') as fin:
        word2id = json.load(fin)

    print('Loading the model SAN1')
    graph_SAN1 = load_graph("data/" + target_timestamp + "/runs/models/frozen_model_SAN1.pb")
    input_x1_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN1, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN1 = tf.Session(graph=graph_SAN1, config=sess_config)

    print('Loading the model SAN2')
    graph_SAN2 = load_graph("data/" + target_timestamp + "/runs/models/frozen_model_SAN2.pb")
    input_x1_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN2, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN2 = tf.Session(graph=graph_SAN2, config=sess_config)

    print('Loading the model SAN3')
    graph_SAN3 = load_graph("data/" + target_timestamp + "/runs/models/frozen_model_SAN3.pb")
    input_x1_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN3, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN3 = tf.Session(graph=graph_SAN3, config=sess_config)

    print('Loading the model SAN4')
    graph_SAN4 = load_graph("data/" + target_timestamp + "/runs/models/frozen_model_SAN4.pb")
    input_x1_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN4, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN4 = tf.Session(graph=graph_SAN4, config=sess_config)

    print('Loading the model SAN5')
    graph_SAN5 = load_graph("data/" + target_timestamp + "/runs/models/frozen_model_SAN5.pb")
    input_x1_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN5, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN5 = tf.Session(graph=graph_SAN5, config=sess_config)


if __name__ == "__main__":
    timestamp = '16082018'

    with open(os.path.join('data', timestamp, 'word2id.json'), 'r') as fin:
        word2id = json.load(fin)

    gpu_memory = .2
    gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=gpu_memory)
    sess_config = tf.ConfigProto(gpu_options=gpu_options)

    print('Loading the model SAN1')
    graph_SAN1 = load_graph("data/" + timestamp + "/runs/models/frozen_model_SAN1.pb")
    input_x1_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN1, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN1 = tf.Session(graph=graph_SAN1, config=sess_config)

    print('Loading the model SAN2')
    graph_SAN2 = load_graph("data/" + timestamp + "/runs/models/frozen_model_SAN2.pb")
    input_x1_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN2, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN2 = tf.Session(graph=graph_SAN2, config=sess_config)

    print('Loading the model SAN3')
    graph_SAN3 = load_graph("data/" + timestamp + "/runs/models/frozen_model_SAN3.pb")
    input_x1_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN3, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN3 = tf.Session(graph=graph_SAN3, config=sess_config)

    print('Loading the model SAN4')
    graph_SAN4 = load_graph("data/" + timestamp + "/runs/models/frozen_model_SAN4.pb")
    input_x1_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN4, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN4 = tf.Session(graph=graph_SAN4, config=sess_config)

    print('Loading the model SAN5')
    graph_SAN5 = load_graph("data/" + timestamp + "/runs/models/frozen_model_SAN5.pb")
    input_x1_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN5 = graph_SAN5.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN5, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN5 = tf.Session(graph=graph_SAN5, config=sess_config)

    print('Starting the REST API')
    app.run(host='0.0.0.0')
