import tensorflow.compat.v1 as tf
tf.compat.v1.disable_v2_behavior()


import numpy as np

from claid import CLAID
from claid.module import Module

from claid.dispatch.proto.sensor_data_types_pb2 import *
from claid.dispatch.proto.claidservice_pb2 import *

from claid.logger import Logger

from human_activity_recognition.human_activity_recognizer import HumanActivityRecognizer


class HARModule(Module):
    @staticmethod
    def annotate_module(annotator):
        annotator.set_module_category("Custom")
        annotator.set_module_description("A Module allowing to do human activity recognition using machine learning.")

        annotator.describe_subscribe_channel("AccelerationInputData", AccelerationData(), "Acceleration input data")
        annotator.describe_subscribe_channel("GyroscopeInputData", GyroscopeData(), "Gyroscope input data")
        annotator.describe_publish_channel("OutputDataLabel", str(), "Output data")


    def __init__(self):
        super().__init__()
        self.ctr = 0

    def initialize(self, properties):

        print("HARModule init 1")
        self.subscribe("AccelerationInputData", AccelerationData(), self.on_acceleration_data)
        self.subscribe("GyroscopeInputData", GyroscopeData(), self.on_gyroscope_data)

        self.output_channel = self.publish("OutputDataLabel", str())
        print("HARModule init 2")

        self.required_samples = 5 * 20 # 5 seconds times 20 Hertz
        # 1 person, 200 samples (10 * 20Hz), 3 axis
        self.acc_xs = list()
        self.acc_ys = list()
        self.acc_zs = list()

        self.gyro_xs = list()
        self.gyro_ys = list()
        self.gyro_zs = list()
        print("HARModule init 3")

        self.recognizer = HumanActivityRecognizer()
        print("HARModule init 4")
        self.recognizer.initialize_recognizer()
        print("HARModule init 5")



    def on_acceleration_data(self, data):
        print("HARModule init 6")

        acceleration_data = data.get_data()

        for sample in acceleration_data.samples:
            self.acc_xs.append(np.float32(sample.acceleration_x))
            self.acc_ys.append(np.float32(sample.acceleration_y))
            self.acc_zs.append(np.float32(sample.acceleration_z))

        if len(self.acc_xs) > self.required_samples:
            self.acc_xs = self.acc_xs[-self.required_samples:]
            self.acc_ys = self.acc_ys[-self.required_samples:]
            self.acc_zs = self.acc_zs[-self.required_samples:]

        self.run_inference_if_enough_data()

    def on_gyroscope_data(self, data):

        gyroscope_data = data.get_data()

        for sample in gyroscope_data.samples:
            self.gyro_xs.append(np.float32(sample.gyroscope_x))
            self.gyro_ys.append(np.float32(sample.gyroscope_y))
            self.gyro_zs.append(np.float32(sample.gyroscope_z))

        if len(self.gyro_xs) > self.required_samples:
            self.gyro_xs = self.gyro_xs[-self.required_samples:]
            self.gyro_ys = self.gyro_ys[-self.required_samples:]
            self.gyro_zs = self.gyro_zs[-self.required_samples:]


        self.run_inference_if_enough_data()

    def run_inference_if_enough_data(self):
        # Downstairs	Jogging	  Sitting	Standing	Upstairs	Walking
        # Generate random float data
        print("running inference ", len(self.acc_xs), len(self.gyro_xs))
        print("HARModule init 7")

        if(len(self.acc_xs) >= self.required_samples and len(self.gyro_xs) >= self.required_samples):
            print("HARModule init 8")

            output_data = self.recognizer.run_inference(np.array([self.acc_xs, self.acc_ys, self.acc_zs]),\
                                                        np.array([self.gyro_xs, self.gyro_ys, self.gyro_zs]))
            label = self.recognizer.get_label(output_data)

            self.output_channel.post(label + " " + str(self.ctr))

            self.acc_xs = self.acc_xs[-60:]
            self.acc_ys = self.acc_ys[-60:]
            self.acc_zs = self.acc_zs[-60:]

            self.gyro_xs = self.gyro_xs[-60:]
            self.gyro_ys = self.gyro_ys[-60:]
            self.gyro_zs = self.gyro_zs[-60:]


            self.ctr += 1