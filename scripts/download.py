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

# HORI = ['W', 'E']
# VERT = ['N', 'S']
HORI = ['W']
VERT = ['N']
RATE = 0.1

def scrape_objects(room_name):
    response = api.get('game/room-objects', shard='shard0', room=room_name)
    if 'ok' in response:
        objects = []
        for possible in response['objects']:
            if possible['type'] in ('controller', 'mineral', 'source'):
                objects.append({key: possible[key] for key in ('type', 'x', 'y')})
        return objects
    else:
        raise RuntimeError(f'Unexpected response:\n{response}')

def scrape_terrain(room_name):
    response = api.room_terrain(room_name, True)
    # if vert_idx < 3 and hori_idx < 2:
    #     print(f'Got room {column_name}{vert_idx}')
    #     response = {'ok': 1, 'terrain': [{'terrain': f'Terrain for {column_name}{vert_idx}'}]}
    # else:
    #     response = {'error': 'invalid room'}

    if 'ok' in response:
       return response['terrain'][0]['terrain']
    else:
        raise RuntimeError(f'Unexpected response:\n{response}')

def scrape(hori_dir, vert_dir, data_dir, scrape_fn, map_dim=91):
    last_request = time()
    data_dir.mkdir(exist_ok=True)

    hori_start = max((int(path.name.split('.', 1)[0]) for path in data_dir.iterdir()), default=None)

    if hori_start is not None:
        resume_data = read_data(data_dir / f'{hori_start}.json.xz')
    else:
        resume_data = None
        hori_start = 0

    for hori_idx in range(hori_start, map_dim):
        column_name = f'{hori_dir}{hori_idx}{vert_dir}'
        if resume_data is not None:
            data = resume_data
            assert data['column'] == column_name
            vert_start = len(data['rooms'])
            resume_data = None
            print(f'Resuming {hori_dir}{hori_idx}{vert_dir}')
        else:
            data = {'column': column_name, 'rooms': []}
            vert_start = 0
            print(f'Starting {hori_dir}{hori_idx}{vert_dir}')

        stop = False
        with tqdm(total=map_dim) as pbar:
            pbar.update(vert_start)
            try:
                for vert_idx in range(vert_start, map_dim):
                    pbar.set_description(f'{column_name}{vert_idx}')
                    elapsed = time() - last_request
                    if elapsed < RATE:
                        sleep(RATE - elapsed)
                    data['rooms'].append(scrape_fn(f'{column_name}{vert_idx}'))
                    last_request = time()
                    pbar.update(1)
            except KeyboardInterrupt:
                stop = True
        if len(data['rooms']) > 0:
            write_data(data_dir / f'{hori_idx}.json.xz', data)

        if stop:
            break

scrape('W', 'N', data_dir / 'objects', scrape_objects)
# terrain = api.room_terrain('W0N0', True, shard='shard3')
# info = api.shard_info()
# print(terrain)