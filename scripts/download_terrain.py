import json
from pathlib import Path
from time import sleep, time
import screepsapi
import lzma
from tqdm import tqdm

def read_data(path):
    with lzma.open(path, 'rt') as handle:
        return json.load(handle)

def write_data(path, data):
    with lzma.open(path, 'wt') as handle:
        json.dump(data, handle)

with open('../gradle.properties', 'rt') as handle:
    token = handle.readline().split('=', 1)[1].strip()

data_dir = Path('../data')
data_dir.mkdir(exist_ok=True)

api = screepsapi.API(token=token)
last_request = time()

# HORI = ['W', 'E']
# VERT = ['N', 'S']
HORI = ['W']
VERT = ['N']

for hori_dir in HORI:
    for vert_dir in VERT:
        sub_folder = data_dir / f'{hori_dir}{vert_dir}'
        sub_folder.mkdir(exist_ok=True)

        hori_idx = max((int(path.name.split('.', 1)[0]) for path in sub_folder.iterdir()), default=None)
        hori_continue = True
        vert_len = None

        if hori_idx is not None:
            resume_data = read_data(sub_folder / f'{hori_idx}.json.xz')
        else:
            resume_data = None
            hori_idx = 0

        while hori_continue:
            column_name = f'{hori_dir}{hori_idx}{vert_dir}'
            if resume_data is not None:
                data = resume_data
                assert data['column'] == column_name
                vert_idx = len(data['rooms'])
                resume_data = None
                print(f'Resuming {hori_dir}{hori_idx}{vert_dir}')
            else:
                data = {'column': column_name, 'rooms': []}
                vert_idx = 0
                print(f'Starting {hori_dir}{hori_idx}{vert_dir}')

            with tqdm(total=vert_len) as pbar:
                try:
                    while vert_len is None or vert_idx < vert_len:
                        pbar.set_description(f'{column_name}{vert_idx}')
                        elapsed = time() - last_request
                        if elapsed < 1:
                            sleep(1 - elapsed)
                        response = api.room_terrain(f'{column_name}{vert_idx}', True)
                        last_request = time()
                        # if vert_idx < 3 and hori_idx < 2:
                        #     print(f'Got room {column_name}{vert_idx}')
                        #     response = {'ok': 1, 'terrain': [{'terrain': f'Terrain for {column_name}{vert_idx}'}]}
                        # else:
                        #     response = {'error': 'invalid room'}

                        if 'ok' in response:
                            data['rooms'].append(response['terrain'][0]['terrain'])
                        elif 'error' in response and response['error'] == 'invalid room':
                            if vert_len is None:
                                vert_len = vert_idx
                            else:
                                hori_continue = False
                                break
                        else:
                            print(f'Unexpected response:\n{response}')
                            hori_continue = False
                            break
                        vert_idx += 1
                        pbar.update(1)
                except KeyboardInterrupt:
                    hori_continue = False
            if len(data['rooms']) > 0:
                write_data(sub_folder / f'{hori_idx}.json.xz', data)
            hori_idx += 1


# terrain = api.room_terrain('W0N0', True, shard='shard3')
# info = api.shard_info()
# print(terrain)