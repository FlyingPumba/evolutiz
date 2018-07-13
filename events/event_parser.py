from events.all_events import event_classes

def parse_event(line):
    index1 = line.find('(')
    index2 = line.find(')')
    if index1 < 0 or index2 < 0:
        return None

    cmd = line[:index1]
    args = line[index1+1:index2].split(",")
    args = map(lambda x: x.trim(), args)

    for event_class in event_classes:
        result = event_class.parse(cmd, args)
        if result is not None:
            return result

    return None