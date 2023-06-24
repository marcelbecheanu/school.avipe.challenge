# -*- coding: utf-8 -*-

from __future__ import division, print_function
# coding=utf-8
import sys
import os
import glob
import re
import numpy as np
import tensorflow as tf

from tensorflow.compat.v1 import ConfigProto
from tensorflow.compat.v1 import InteractiveSession

config = ConfigProto()
config.gpu_options.per_process_gpu_memory_fraction = 0.5
config.gpu_options.allow_growth = True
session = InteractiveSession(config=config)
# Keras
from tensorflow.keras.applications.resnet50 import preprocess_input
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image

from werkzeug.utils import secure_filename
import shutil

MODEL_PATH ='model_inception.h5'
model = load_model(MODEL_PATH)
def classify(img_path):
    print(img_path)
    img = image.load_img(img_path, target_size=(224, 224))

    x = image.img_to_array(img)
    x=x/255
    x = np.expand_dims(x, axis=0)

    preds = model.predict(x)
    preds=np.argmax(preds, axis=1)
    if preds==0:
        preds="Black Rot"
    elif preds==1:
        preds="Esca"
    elif preds==2:
        preds="Leaf blight"
    elif preds==3:
        preds="Healthy"
    else:
        preds="Healthy"
    print(preds)

    #change the dir of image to use in future train
    dirCache = os.getcwd() + '\\learn'
    if(os.path.isdir(dirCache) == 0):
        os.mkdir(dirCache)
    d = os.getcwd() + '\\learn\\'+preds
    if(os.path.isdir(d) == 0):
        os.mkdir(d)

    shutil.move(img_path, d)

    return '{"classification": "'+preds+'"}'
