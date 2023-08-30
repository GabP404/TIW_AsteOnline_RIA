'use strict';

function makeCall(method, url, form, onloadCallback) {
    let req = new XMLHttpRequest();

    req.onload = () => {
        if(req.status === 401){
            window.location.href = 'index.html';
        } else {
            onloadCallback(req);
        }
    };

    req.open(method, url);
    req.setRequestHeader('Accept', 'application/json');

    if(form != null){
        req.send(new FormData(form));
    } else {
        req.send();
    }
}

function formatDate(date){
    return new Date(date).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}


function timeBetween(date1, date2) {
    const duration = date2 - date1;
    const durationInSeconds = Math.floor(duration / 1000);
    const durationInMinutes = Math.floor(durationInSeconds / 60);
    const durationInHours = Math.floor(durationInMinutes / 60);
    const durationInDays = Math.floor(durationInHours / 24);

    if (duration < 0) {
        return "Asta scaduta!";
    }

    if (durationInHours === 0) {
        return "Meno di 1 ora";
    }

    let hours;
    if (durationInHours % 24 === 1) {
        hours = "1 ora";
    } else {
        hours = `${durationInHours % 24} ore`;
    }

    if (durationInDays === 0) {
        return hours;
    }

    let days;
    if (durationInDays === 1) {
        days = "1 giorno";
    } else {
        days = `${durationInDays} giorni`;
    }

    if (durationInHours % 24 === 0) {
        return days;
    } else {
        return `${days} e ${hours}`;
    }
}

function calculateTimeBetween(date1, date2) {
    const duration = date2 - date1;
    const differenceInDays = Math.floor(duration / 1000 / 60 / 60 / 24);
    const differenceInHours = Math.floor(duration / 1000 / 60 / 60) - differenceInDays * 24;
    const differenceInMinutes = Math.floor(duration / 1000 / 60) - differenceInDays * 24 * 60 - differenceInHours * 60;
    const differenceInSeconds = Math.floor(duration / 1000) - differenceInDays * 24 * 60 * 60 - differenceInHours * 60 * 60 - differenceInMinutes * 60;


let time = "";

    if (duration < 0) {
        return "Asta scaduta!";
    }

    if(differenceInDays == 1){
        time += differenceInDays + " giorno ";
    }else if(differenceInDays > 1){
        time += differenceInDays + " giorni ";
    }
    if(differenceInHours == 1){
        time += differenceInHours + " ora ";
    }else if(differenceInHours > 1){
        time += differenceInHours + " ore ";
    }
    if(differenceInMinutes == 1){
        time += differenceInMinutes + " minuto ";
    } else if(differenceInMinutes > 1){
        time += differenceInMinutes + " minuti ";
    }
    if(differenceInSeconds == 1){
        time += differenceInSeconds + " secondo ";
    } else if(differenceInSeconds > 1){
        time += differenceInSeconds + " secondi ";
    }
    return time;
}

function saveRecentAuction(id) {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        savedAuctions = [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    // Check if the auction is already in the list
    if(savedAuctions.indexOf(id) !== -1){
        return;
    }
    savedAuctions.push(id);

    // If there are more than 5 auctions saved, remove the oldest one
    if (savedAuctions.length > 5) {
        savedAuctions.shift();
    }

    localStorage.setItem('saved_auctions', JSON.stringify(savedAuctions));
    localStorage.setItem('last_action_time', new Date().getTime().toString());
}


function getRecentAuctions() {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        return [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    return savedAuctions;
}

function removeRecentAuction(id) {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        return;
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    if(savedAuctions.indexOf(id) !== -1){
        savedAuctions.splice(savedAuctions.indexOf(id), 1);
    }

    localStorage.setItem('saved_auctions', JSON.stringify(savedAuctions));
}


function saveUser(user) {
    let user_id = user.id;
    if(
        user_id !== parseInt(localStorage.getItem('user_id')) ||
        new Date().getTime() - parseInt(localStorage.getItem('last_action_time')) > 1000 * 60 * 60 * 24 * 30
    ){
        localStorage.clear();
    }
    localStorage.setItem('user_id', user_id);
    localStorage.setItem('firstname', user.firstname);
}

function logout() {
    localStorage.removeItem('user_id');
    localStorage.removeItem('firstname');
}

function saveUserAfterLogin(user) {
    let user_id = user.id;
    let key = user_id + + "_" +"last_action_time";
    localStorage.setItem('user_id', user_id);
    localStorage.setItem('firstname', user.firstname);

    let result = localStorage.getItem(key);
    if (result != null) {
        if( new Date().getTime() - parseInt(localStorage.getItem(key) > 1000 * 60 * 60 * 24 * 30 )){
            localStorage.removeItem(key);
        }
    }
}

function removeViewedAuction(id) {
    let user_id = localStorage.getItem('user_id');
    let key = user_id + + "_" +"saved_auctions";
    let savedAuctions = localStorage.getItem(key);
    if(savedAuctions === null){
        return;
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    if(savedAuctions.indexOf(id) !== -1){
        savedAuctions.splice(savedAuctions.indexOf(id), 1);
    }

    localStorage.setItem(key, JSON.stringify(savedAuctions));
}

function saveViewedAuction(id) {
    let user_id = localStorage.getItem('user_id');
    let key_saved_auctions = user_id + + "_" +"saved_auctions";
    let key_last_action_time = user_id + + "_" +"last_action_time";
    let savedAuctions = localStorage.getItem(key_saved_auctions);
    if(savedAuctions === null){
        savedAuctions = [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    // Check if the auction is already in the list
    if(savedAuctions.indexOf(id) !== -1){
        return;
    }
    savedAuctions.push(id);

    // If there are more than 5 auctions saved, remove the oldest one
    if (savedAuctions.length > 5) {
        savedAuctions.shift();
    }

    localStorage.setItem(key_saved_auctions, JSON.stringify(savedAuctions));
    localStorage.setItem(key_last_action_time, new Date().getTime().toString());
}


function getViewedAuctions() {
    let user_id = localStorage.getItem('user_id');
    let key_saved_auctions = user_id + + "_" +"saved_auctions";
    let savedAuctions = localStorage.getItem(key_saved_auctions);
    if(savedAuctions === null){
        return [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    return savedAuctions;
}


