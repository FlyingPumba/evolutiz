import pickle

import matplotlib.pyplot as plt
import networkx

import settings


def plot(history, apk_dir):
    graph = networkx.DiGraph(history.genealogy_tree)
    # graph = graph.reverse()     # Make the grah top-down
    # colors = [toolbox.evaluate(history.genealogy_history[i])[0] for i in graph]
    # networkx.draw(graph, node_color=colors)
    # networkx.draw(graph)
    # plt.show()
    networkx.write_dot(graph, apk_dir + '/intermediate/tmp.dot')
    # same layout using matplotlib with no labels
    plt.title("History Network")
    pos = networkx.graphviz_layout(graph, prog='dot', scale=10)
    networkx.draw(graph, pos, with_labels=False, arrows=False, node_size=30)

    fig = plt.gcf()
    fig.set_size_inches(18.5, 10.5)
    fig.savefig(apk_dir + '/intermediate/history_network.pdf', dpi=300)


if __name__ == "__main__":
    print("Test")
    history_pickle = open(settings.WORKING_DIR + "intermediate/history.pickle")
    history = pickle.load(history_pickle)
    plot(history)
