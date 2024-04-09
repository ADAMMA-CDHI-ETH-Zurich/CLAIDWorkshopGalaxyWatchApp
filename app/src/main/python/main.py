#***************************************************************************
# Copyright (C) 2023 ETH Zurich
# CLAID: Closing the Loop on AI & Data Collection (https://claid.ethz.ch)
# Core AI & Digital Biomarker, Acoustic and Inflammatory Biomarkers (ADAMMA)
# Centre for Digital Health Interventions (c4dhi.org)
 
# Authors: Patrick Langer
 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 
#         http://www.apache.org/licenses/LICENSE-2.0
 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#**************************************************************************/

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