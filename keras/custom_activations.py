# -*- coding: utf-8 -*-
from __future__ import absolute_import

from .. import initializers
from .. import regularizers
from .. import constraints
from ..engine import Layer
from ..engine import InputSpec
from .. import backend as K
from ..legacy import interfaces
import numpy as np


class Radial(Layer):
    """A custom activation function for Radial Basis Function Neural Networks.  
    """

    def __init__(self, alpha=0.3, **kwargs):
        super(Radial, self).__init__(**kwargs)
        self.supports_masking = True
        self.alpha = K.cast_to_floatx(alpha)

    def call(self, inputs):        
        return np.exp(-1*np.square(inputs)/(2.0*float(self.alpha)*float(self.alpha)))

    def get_config(self):
        config = {'alpha': self.alpha}
        base_config = super(Radial, self).get_config()
        return dict(list(base_config.items()) + list(config.items()))