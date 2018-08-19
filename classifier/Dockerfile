FROM python:2.7.15

ADD . /
RUN pip install -r requirements.txt

EXPOSE 5000
CMD ["python", "serve.py"]