The app uses docker-compose, to run u should
```
docker-compose up
```
This app joins covid cases streets with real estate offers data and saves to db top 10 cheapest real estate offers by price per square meter

Covid cases data is refreshed once a day, so it is fetched daily
Top 10 listing is saved every hour and on demand

Available endpoints:

POST 127.0.0.1:8080/run - start job that joins covid cases 

GET 127.0.0.1:8080/last - get last listing

GET 127.0.0.1:8080/today - get listings for today