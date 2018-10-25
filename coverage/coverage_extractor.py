from lxml import html


def extract_coverage(path):
    tree = html.parse(path)
    coverage = tree.getroot().xpath('/html/body/table[2]/tr[2]/td[5]/text()')[0].strip()
    return coverage