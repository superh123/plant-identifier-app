from PIL import Image

import os
import cv2
from flask import Flask, jsonify, request
from werkzeug.datastructures import FileStorage

#dhash implementation from benhoyt

app = Flask("dhash")


def _get_grays_pil(image, width, height, fill_color='white'):

    # Convert image to grayscale
    image = image.convert('L')  
    # Resize to desire size
    image = image.resize((width, height))
    # Return list of grayscale pixel values which represents image
    return list(image.getdata())


def dhash_row_col(image, size=8):

    # dhash compares adjacent pixels, so we need 1 extra pixel in each direction
    width = size + 1

    #convert image to grayscale, resize to 9x9=81 and return list of pixel values
    grays = _get_grays_pil(image, width, width)

    row_hash = 0
    col_hash = 0

    #loop over image grid i.e. the list
    for y in range(size): # rows 0-7
        for x in range(size): # cols 0-7
            offset = y * width + x # index of current pixel in flattened grayscale list

            row_bit = grays[offset] < grays[offset + 1] # is the next grey value grater than current?
            row_hash = row_hash << 1 | row_bit # shift existing bits left, and if row_bit true add 1 else 0 using BITWISE OR

            col_bit = grays[offset] < grays[offset + width]
            col_hash = col_hash << 1 | col_bit
    
    return (row_hash, col_hash)

@app.route("/hasher", methods=['POST'])
def dhash_int(size=8):

    # If file isn't in request, return a 400 error
    if 'file' not in request.files:
        return 'File not in request', 400
    
    image = request.files['file']

    # Ensure image is actually file object and has a name
    if isinstance(image, FileStorage) and image.filename:
        image = Image.open(image.stream)
        row_hash, col_hash = dhash_row_col(image, size=size)
        return str(row_hash << (size*size) | col_hash)
    else:
        return "Invalid file", 400


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
    




    

