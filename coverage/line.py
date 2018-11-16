class Line(object):

    def __init__(self, package_name, class_name, line_number, code, line_coverage_status):
        self.package_name = package_name
        self.class_name = class_name
        self.line_number = line_number
        self.code = code
        self.line_coverage_status = line_coverage_status