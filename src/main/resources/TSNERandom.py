import numpy as np
import sys

if len(sys.argv) < 4:
    print('python3 TSNERandom.py [seed] [n_samples] [new_dim]')
else:
    seed = int(sys.argv[1])
    n_samples = int(sys.argv[2])
    new_dim = int(sys.argv[2])
    rs = np.random.RandomState(seed)
    initial_embed = 1e-4 * rs.randn(n_samples, new_dim).astype(np.float32)
    np.savetxt('random_inital_values_for_tsne.csv', initial_embed, fmt='%.10f', delimiter=',')
