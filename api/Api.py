from flask import Flask
from flask import request
from flask import jsonify
import scipy
from scipy.io import wavfile
import scipy.stats as st
import numpy as np
import math
from numpy.lib.stride_tricks import as_strided
from sklearn.model_selection import StratifiedShuffleSplit

app = Flask(__name__)

fpaths = []
labels = []
spoken = []

def train():
    global fpaths
    global labels
    global spoken

    print("spoken = ", spoken)
    print("labels = ", labels)
    data = np.zeros((len(fpaths), 32000))#data list untuk menampung sementara

    maxsize = -1 #size yang terbesar

    for n,file in enumerate(fpaths):
        _, d = wavfile.read(file)
        data[n, :d.shape[0]] = d
        
        if d.shape[0] > maxsize:
            maxsize = d.shape[0]
    data = data[:, :maxsize] #agar panjang data sama

    #Each sample file is one row in data, and has one entry in labels
    all_labels = np.zeros(data.shape[0])
    for n, l in enumerate(set(labels)):
        all_labels[np.array([i for i, _ in enumerate(labels) if _ == l])] = n #untuk menandai nama lebel 0,0,1,....

    all_labels.sort()

    k=1
    all_obs = []
    ll=data.shape[0]
    N=math.ceil(ll/10)

    for i in range(ll):
        d = np.abs(stft(data[i, :]))
        dimensi = 6 #ambil 6 potongan, untuk n_peak
        
        obs = np.zeros((dimensi, d.shape[0]))
        for r in range(d.shape[0]):
            _, t = peakfind(d[r, :], n_peaks=dimensi)
            obs[:, r] = t.copy()
        if i % N == 0:
            print("Processed obs %s"  % (10*k), "%")
            k=k+1
        #data dimasukan kedalam array
        all_obs.append(obs)
        
    all_obs = np.atleast_3d(all_obs)

    sss = StratifiedShuffleSplit(n_splits=1, test_size=0.4, random_state=1)

    for n,i in enumerate(all_obs): #Normalisasi, dibagi jumlah semuanya, probabilitas
        all_obs[n] /= all_obs[n].sum(axis=0) 

    for train_index, test_index in sss.split(all_obs,all_labels):
        X_train=all_obs[train_index, ...]
        X_test=all_obs[test_index, ...]
        y_train=all_labels[train_index]
        y_test=all_labels[test_index]
    print('Size of training matrix:', X_train.shape)
    print('Size of testing matrix:', X_test.shape)

    ys = set(all_labels)
    print('Training')
    ms = [gmmhmm(6) for y in ys]
    _ = [m.fit(X_train[y_train == y, :, :]) for m, y in zip(ms, ys)]

    print('Testing')
    ps = [m.transform(X_test) for m in ms]
    res = np.vstack(ps)
    predicted_labels = np.argmax(res, axis=0)

    missed   = (predicted_labels != y_test)
    accurate = 100 * (1 - np.mean(missed))
    print('Test accuracy: %.2f percent' % accurate)
    
    fpaths = []
    labels = []
    spoken = []
    return jsonify(accurate)

def stft(x, fftsize=64, overlap_pct=0.5):   
    #Modified dari http://stackoverflow.com/questions/2459295/stft-and-istft-in-python
    hop = int(fftsize * (1 - overlap_pct))
    
    w = scipy.hanning(fftsize + 1)[:-1]  
    #hanning adalah nilai maksimum dinormalisasi menjadi satu
    #titik puncak yang dibentuk dengan menggunakan kosinus berbobot
    raw = np.array([np.fft.rfft(w * x[i:i + fftsize]) for i in range(0, len(x) - fftsize, hop)])
    return raw[:, :(fftsize // 2)]
    
def peakfind(x, n_peaks, l_size=3, r_size=3, c_size=3, f=np.mean):
    win_size = l_size + r_size + c_size
    shape = x.shape[:-1] + (x.shape[-1] - win_size + 1, win_size)
    strides = x.strides + (x.strides[-1],)
    xs = as_strided(x, shape=shape, strides=strides)
    def is_peak(x):
        centered = (np.argmax(x) == l_size + int(c_size/2))
        l = x[:l_size]
        c = x[l_size:l_size + c_size]
        r = x[-r_size:]
        passes = np.max(c) > np.max([f(l), f(r)])
        if centered and passes:
            return np.max(c)
        else:
            return -1
    r = np.apply_along_axis(is_peak, 1, xs)
    top = np.argsort(r, None)[::-1]
    heights = r[top[:n_peaks]]
    #Add l_size and half - 1 of center size to get to actual peak location
    top[top > -1] = top[top > -1] + l_size + int(c_size / 2.)
    return heights, top[:n_peaks]
class gmmhmm:
    #This class sudah dimodifikasi dari https://code.google.com/p/hmm-speech-recognition/source/browse/Word.m
    def __init__(self, n_states):
        self.n_states = n_states
        self.random_state = np.random.RandomState(0)
        
        #Normalize random initial state
        self.prior = self._normalize(self.random_state.rand(self.n_states, 1))
        self.A = self._stochasticize(self.random_state.rand(self.n_states, self.n_states))
        
        self.mu = None
        self.covs = None
        self.dimensi = None
           
    def _forward(self, B):
        log_likelihood = 0.
        T = B.shape[1]
        alpha = np.zeros(B.shape)
        for t in range(T):
            if t == 0:
                alpha[:, t] = B[:, t] * self.prior.ravel()
            else:
                alpha[:, t] = B[:, t] * np.dot(self.A.T, alpha[:, t - 1])
         
            alpha_sum = np.sum(alpha[:, t])
            alpha[:, t] /= alpha_sum
            log_likelihood = log_likelihood + np.log(alpha_sum)
        return log_likelihood, alpha
    
    def _backward(self, B):
        T = B.shape[1]
        beta = np.zeros(B.shape);
           
        beta[:, -1] = np.ones(B.shape[0])
            
        for t in range(T - 1)[::-1]:
            beta[:, t] = np.dot(self.A, (B[:, t + 1] * beta[:, t + 1]))
            beta[:, t] /= np.sum(beta[:, t])
        return beta
    
    def _state_likelihood(self, obs):
        obs = np.atleast_2d(obs)
        B = np.zeros((self.n_states, obs.shape[1]))
        for s in range(self.n_states):
            #Needs scipy 0.14
            np.random.seed(self.random_state.randint(1))
            B[s, :] = st.multivariate_normal.pdf(
                obs.T, mean=self.mu[:, s].T, cov=self.covs[:, :, s].T)
            #This function can (and will!) return values >> 1
            #See the discussion here for the equivalent matlab function
            #https://groups.google.com/forum/#!topic/comp.soft-sys.matlab/YksWK0T74Ak
            #Key line: "Probabilities have to be less than 1,
            #Densities can be anything, even infinite (at individual points)."
            #This is evaluating the density at individual points...
        return B
    
    def _normalize(self, x):
        return (x + (x == 0)) / np.sum(x)
    
    def _stochasticize(self, x):
        return (x + (x == 0)) / np.sum(x, axis=1)
    
    def _em_init(self, obs):
        #Using this _em_init function allows for less required constructor args
        if self.dimensi is None:
            self.dimensi = obs.shape[0]
        if self.mu is None:
            subset = self.random_state.choice(np.arange(self.dimensi), size=self.n_states, replace=False)
            self.mu = obs[:, subset]
        if self.covs is None:
            self.covs = np.zeros((self.dimensi, self.dimensi, self.n_states))
            self.covs += np.diag(np.diag(np.cov(obs)))[:, :, None]
        return self
    
    def _em_step(self, obs): 
        obs = np.atleast_2d(obs)
        B = self._state_likelihood(obs)
        T = obs.shape[1]
        
        log_likelihood, alpha = self._forward(B)
        beta = self._backward(B)
        
        xi_sum = np.zeros((self.n_states, self.n_states))
        gamma = np.zeros((self.n_states, T))
        
        for t in range(T - 1):
            partial_sum = self.A * np.dot(alpha[:, t], (beta[:, t] * B[:, t + 1]).T)
            xi_sum += self._normalize(partial_sum)
            partial_g = alpha[:, t] * beta[:, t]
            gamma[:, t] = self._normalize(partial_g)
              
        partial_g = alpha[:, -1] * beta[:, -1]
        gamma[:, -1] = self._normalize(partial_g)
        
        expected_prior = gamma[:, 0]
        expected_A = self._stochasticize(xi_sum)
        
        expected_mu = np.zeros((self.dimensi, self.n_states))
        expected_covs = np.zeros((self.dimensi, self.dimensi, self.n_states))
        
        gamma_state_sum = np.sum(gamma, axis=1)
        #Set zeros to 1 before dividing
        gamma_state_sum = gamma_state_sum + (gamma_state_sum == 0)
        
        for s in range(self.n_states):
            gamma_obs = obs * gamma[s, :]
            expected_mu[:, s] = np.sum(gamma_obs, axis=1) / gamma_state_sum[s]
            partial_covs = np.dot(gamma_obs, obs.T) / gamma_state_sum[s] - np.dot(expected_mu[:, s], expected_mu[:, s].T)
            #Symmetrize
            partial_covs = np.triu(partial_covs) + np.triu(partial_covs).T - np.diag(partial_covs)
        
        #Pastikan semidefinite positif, dengan menambahkan pembebanan diagonal
        expected_covs += .01 * np.eye(self.dimensi)[:, :, None]
        
        self.prior = expected_prior
        self.mu = expected_mu
        self.covs = expected_covs
        self.A = expected_A
        return log_likelihood
    
    def fit(self, obs, n_iter=15):
        #Support for 2D and 3D arrays
        #2D should be n_features, dimensi
        #3D should be n_examples, n_features, dimensi
        #For example, with 6 features per speech segment, 290 different words
        #this array should be size
        #(290, 6, X) where X is the number of frames with features extracted
        #For a single example file, the array should be size (6, X)
        if len(obs.shape) == 2:
            for i in range(n_iter):
                self._em_init(obs)
                log_likelihood = self._em_step(obs)
        elif len(obs.shape) == 3:
            count = obs.shape[0]
            for n in range(count):
                for i in range(n_iter):
                    self._em_init(obs[n, :, :])
                    log_likelihood = self._em_step(obs[n, :, :])
        return self
    
    def transform(self, obs):
        #Support for 2D and 3D arrays
        #2D should be n_features, dimensi
        #3D should be n_examples, n_features, dimensi
        #For example, with 6 features per speech segment, 290 different words
        #this array should be size
        #(290, 6, X) where X is the number of frames with features extracted
        #For a single example file, the array should be size (6, X)
        if len(obs.shape) == 2:
            B = self._state_likelihood(obs)
            log_likelihood, _ = self._forward(B)
            return log_likelihood
        elif len(obs.shape) == 3:
            count = obs.shape[0]
            out = np.zeros((count,))
            for n in range(count):
                B = self._state_likelihood(obs[n, :, :])
                log_likelihood, _ = self._forward(B)
                out[n] = log_likelihood
            return out

@app.route('/train', methods=['POST'])
def loadAudio():
    global fpaths
    global labels
    global spoken

    audio  = request.files.getlist('sound[]')
    for row in audio:
        filename = row.filename.split('_')
        fpaths.append(row)
        labels.append(filename[0])
        if filename[0] not in spoken:
            spoken.append(filename[0])

    return train()

app.run(debug=True, host='0.0.0.0', port=5000)