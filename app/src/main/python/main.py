import os
os.environ["PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION"] = "python"


from claid import CLAID

from claid.module.module_factory import ModuleFactory
global claid
import sys
import os
current_file_path = os.path.abspath(__file__)

# Get the directory containing the currently executed Python file
current_directory = os.path.dirname(current_file_path)
sys.path.append(current_directory)

injections_path = "/sdcard/Android/media/org.c4dhi.adamma.claid_workshop_galaxy_watch/injections"

def attach():
    global claid
    claid=CLAID()
    module_factory = ModuleFactory()
    module_factory.register_all_modules_found_in_path(injections_path)
    socket_path = "unix:///data/user/0/org.c4dhi.adamma.claid_workshop_galaxy_watch/files/claid_local.grpc"
    claid.attach_python_runtime(socket_path, module_factory)
    claid.process_runnables_blocking()