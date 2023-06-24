import os
from classify import classify
from flask import Flask, flash, request, redirect, url_for
from werkzeug.utils import secure_filename

#from trainning import train

UPLOAD_FOLDER = os.getcwd() + '\\uploads'
ALLOWED_EXTENSIONS = { 'png', 'jpg', 'jpeg' }

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS



@app.route('/', methods=['POST'])   
def upload_file():
    if request.method == 'POST':
        print(request.files)

        if 'file' not in request.files:
              return "invalid file" 
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            result = classify(UPLOAD_FOLDER + "\\" + filename)
            return result




if __name__ == '__main__':
    app.run(debug=True, port=8000)