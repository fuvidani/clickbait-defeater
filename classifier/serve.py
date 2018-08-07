import time

import tensorflow as tf
from flask import Flask, request
from flask_cors import CORS
from utils import *

app = Flask(__name__)
cors = CORS(app)


@app.route("/api/predict", methods=['POST'])
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


if __name__ == "__main__":
    with open(os.path.join('data', 'word2id.json'), 'r') as fin:
        word2id = json.load(fin)

    gpu_memory = .2
    gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=gpu_memory)
    sess_config = tf.ConfigProto(gpu_options=gpu_options)

    print('Loading the model SAN1')
    graph_SAN1 = load_graph("data/runs/0715/checkpoints/frozen_model_SAN1.pb")
    input_x1_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN1 = graph_SAN1.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN1, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN1 = tf.Session(graph=graph_SAN1, config=sess_config)

    print('Loading the model SAN2')
    graph_SAN2 = load_graph("data/runs/0715/checkpoints/frozen_model_SAN2.pb")
    input_x1_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN2 = graph_SAN2.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN2, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN2 = tf.Session(graph=graph_SAN2, config=sess_config)

    print('Loading the model SAN3')
    graph_SAN3 = load_graph("data/runs/0715/checkpoints/frozen_model_SAN3.pb")
    input_x1_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN3 = graph_SAN3.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN3, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN3 = tf.Session(graph=graph_SAN3, config=sess_config)

    print('Loading the model SAN4')
    graph_SAN4 = load_graph("data/runs/0715/checkpoints/frozen_model_SAN4.pb")
    input_x1_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text:0")
    input_x1_len_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/post_text_len:0")
    dropout_rate_cell_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/dropout_rate_cell:0")
    batch_size_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/batch_size:0")
    output_prediction_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/prediction:0")
    output_distribution_SAN4 = graph_SAN4.get_tensor_by_name("clickbait/distribution:0")

    print('Starting Session SAN4, setting the GPU memory usage to %f' % gpu_memory)
    sess_SAN4 = tf.Session(graph=graph_SAN4, config=sess_config)

    print('Loading the model SAN5')
    graph_SAN5 = load_graph("data/runs/0715/checkpoints/frozen_model_SAN5.pb")
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
