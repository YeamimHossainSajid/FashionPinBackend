from PIL import Image
import numpy as np
from app.postprocessing.compositor import Compositor

def test_compositor_seamless():
    person_img = Image.new("RGB", (200, 200), color=(255, 0, 0)) # Red
    gen_img = Image.new("RGB", (200, 200), color=(0, 255, 0))     # Green

    protected_mask = np.zeros((200, 200), dtype=np.uint8)
    protected_mask[0:50, 0:50] = 255 # Top-left protected

    res = Compositor.composite(person_img, gen_img, protected_mask)
    res_np = np.array(res)

    assert res.size == (200, 200)
    # Top-left should retain red channel
    assert res_np[10, 10, 0] > 100
