FROM python:2.7.15

ADD . /
RUN pip install -r requirements.txt

EXPOSE 9090
CMD ["python", "serve.py"]