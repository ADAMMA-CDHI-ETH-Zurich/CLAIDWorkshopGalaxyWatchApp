import os
os.environ["PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION"] = "python"


from claid import CLAID

from claid.module.module_factory import ModuleFactory
from claid.logger.logger import Logger
global claid
import sys
import os
current_file_path = os.path.abspath(__file__)

# Get the directory containing the currently executed Python file
current_directory = os.path.dirname(current_file_path)
sys.path.append(current_directory)


def attach(socket_path, injections_path):
    global claid
    Logger.log_warning("Python runtime paths: {}, {}".format(socket_path, injections_path))
    claid = CLAID()
    module_factory = ModuleFactory()
    module_factory.register_all_modules_found_in_path(injections_path)
    claid.attach_python_runtime(socket_path, module_factory)
    claid.process_runnables_blocking()