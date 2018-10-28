import tensorflow as tf


def freeze_graph(model_dir, output_node_names):
    """
    Generates and stores frozen graph from TensorFlow meta graph.

    :param model_dir: str
        Path to TensorFlow meta graph.
    :param output_node_names: list of str
        List of output node names to freeze.
    :return: GraphDef
        GraphDef containing a simplified version of the original.
    """
    checkpoints_path = model_dir + "/checkpoints"
    models_path = model_dir + "/models"
    for model in ["SAN1", "SAN2", "SAN3", "SAN4", "SAN5"]:
        output_graph = models_path + "/frozen_model_" + model + ".pb"

        # We start a session using a temporary fresh Graph
        with tf.Session(graph=tf.Graph()) as sess:
            # We import the meta graph in the current default Graph
            saver = tf.train.import_meta_graph(checkpoints_path + "/" + model + '.meta', clear_devices=True)

            # We restore the weights
            saver.restore(sess, checkpoints_path + "/" + model)

            # We use a built-in TF helper to export variables to constants
            output_graph_def = tf.graph_util.convert_variables_to_constants(
                sess,  # The session is used to retrieve the weights
                tf.get_default_graph().as_graph_def(),  # The graph_def is used to retrieve the nodes
                output_node_names.split(",")  # The output node names are used to select the usefull nodes
            )

            # Finally we serialize and dump the output graph to the filesystem
            with tf.gfile.GFile(output_graph, "wb") as f:
                f.write(output_graph_def.SerializeToString())
            print("%d ops in the final graph." % len(output_graph_def.node))

    return output_graph_def
