'use strict';

{
    // Orchestrators
    let pageOrchestrator = new PageOrchestrator();
    let search = new Search();
    let auctionDetails = new AuctionDetails({
        _auctionDiv: window.document.getElementById('auction-details'),
    });
    let wonAuctions = new WonAuctions({
        _wonAuctionsDiv: window.document.getElementById('won-auctions'),
    });
    let viewedAuctions = new ViewedAuctions({
        _viewedAuctionsDiv: window.document.getElementById('viewed-auctions'),
    });
    let userAuctions = new UserAuctions({
        _userAuctionsDiv: window.document.getElementById('user-auctions'),
    });
    let addItem = new AddItem({
        _addItemForm: window.document.getElementById('add-item-form'),
    });
    let newAuction = new NewAuction({
        _newAuctionForm: window.document.getElementById('new-auction-form'),
    });

    let offersAuctions = new OffersAuctions({
        _offersAuctionsDiv: window.document.getElementById('offers-auctions'),
    });


    // On load
    window.addEventListener('load', () => {
        pageOrchestrator.start();
    });

    function OffersAuctions(elements) {
        this.div = elements._offersAuctionsDiv;
        this.closeButton = this.div.querySelector('.auction-close');
        this.idElement = this.div.querySelector('.auction-id');
        this.statusElement = this.div.querySelector('.auction-status');
        this.startingPriceElement = this.div.querySelector('.auction-starting-price');
        this.deadlineElement = this.div.querySelector('.auction-deadline');
        this.maxOfferElement = this.div.querySelector('.auction-max-offer');
        this.minimumRiseElement = this.div.querySelector('.auction-minimum-rise');
        this.itemsTable = this.div.querySelector('.auction-items-table').querySelector('tbody');
        this.offersTable = this.div.querySelector('.auction-offer-table');
        this.offersTbody = this.offersTable.querySelector('tbody');
        this.divOffer = this.div.querySelector('#auction-offer');
        this.offerForm = this.divOffer.querySelector('form');
        this.updatingMessage = this.div.querySelector('.updating-message');
        this.timeout = null;


        this.setup = function() {
            this.hide();
            this.closeButton.addEventListener('click', (e) => {
                e.preventDefault();
                this.close();
            });

            this.offerForm.addEventListener('submit', (e) => {
                e.preventDefault();

                makeCall('POST', 'CreateOffer', this.offerForm, (req) => {
                    switch (req.status) {
                        case 200:
                            break;
                        default:
                            try {
                                let response = JSON.parse(req.responseText);
                                this.divOffer.querySelector('.error-message').textContent = response.detailMessage;
                            } catch (e) {
                                this.divOffer.querySelector('.error-message').textContent = "An error occurred.";
                            }
                            break;
                    }
                    this.load(this.offerForm.querySelector('input[name="auction_id"]').value);
                });
            });
        }

        this.show = function() {
            this.div.style.display = 'block';
        }

        this.close = function() {
            this.disableTimeout();
            this.hide();
            this.clear();
        }

        this.disableTimeout = function() {
            clearTimeout(this.timeout);
            this.timeout = null;
        }

        this.hide = function() {
            this.div.style.display = 'none';
        }

        this.clear = function() {
            this.idElement.textContent = '';
            this.statusElement.textContent = '';
            this.startingPriceElement.textContent = '';
            this.deadlineElement.textContent = '';
            this.maxOfferElement.textContent = '';
            this.minimumRiseElement.textContent = '';
            this.itemsTable.innerHTML = '';
            this.offersTbody.innerHTML = '';
            this.divOffer.style.display = 'none';
            this.updatingMessage.style.display = 'none';
        }

        this.load = function(auctionId) {
            makeCall('GET', 'OffersAuction?auction_id=' + auctionId, null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.clear();
                        this.updateData(response);
                        this.show();
                        this.divOffer.style.display = 'block';
                        this.offerForm.reset();
                        this.offerForm.querySelector('input[name="auction_id"]').value = response.id;
                        this.offerForm.querySelector('input[name="offer"]').min = response.minOfferToMake;
                        this.disableTimeout();
                        this.timeout = setTimeout(() => {
                            this.load(auctionId);
                        }, 10000);

                        this.updatingMessage.style.display = 'block';

                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            alert(response.detailMessage);
                        } catch (e) {
                            alert("An error occurred.")
                        }
                }
            });
        }

        this.addItem = function(item) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            let img = document.createElement('img');
            img.src = "GetImage/" + item.imagePath;
            img.alt = item.name;
            td.appendChild(img);
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.code;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.name;
            tr.appendChild(td);

            td = document.createElement('td');
            if (item.description === undefined){
                td.textContent = "N/D";
            } else {
                td.textContent = item.description;
            }
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.price + " $" ;
            tr.appendChild(td);

            this.itemsTable.appendChild(tr);
        }

        this.addOffer = function(offer) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            td.textContent = offer.username;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = offer.price + " $";

            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = formatDate(new Date(offer.createdAt));
            tr.appendChild(td);

            this.offersTbody.appendChild(tr);
        }

        this.updateData = function(auction) {
            this.idElement.textContent = auction.id;
            this.statusElement.textContent = (auction.status === 1) ? "Aperta" : "Chiusa";
            this.startingPriceElement.textContent = auction.startingPrice + " $";
            this.deadlineElement.textContent = formatDate(new Date(auction.deadline));
            if(auction.maxOffer === 0) {
                this.maxOfferElement.textContent = "Nessuna offerta";
            } else {
                this.maxOfferElement.textContent = auction.maxOffer + " $";
            }
            this.minimumRiseElement.textContent = auction.minimumRise + " $";

            auction.items.forEach((item) => {
                this.addItem(item);
            });

            if(auction.offers.length > 0) {
                this.div.querySelector('.no-offer-message').style.display = 'none';
                this.offersTable.style.display = 'table';
                auction.offers.forEach((offer) => {
                    this.addOffer(offer);
                });
            } else {
                this.div.querySelector('.no-offer-message').style.display = 'block';
                this.offersTable.style.display = 'none';
            }
        }

    }

    function Search() {
        this.formElement = window.document.getElementById('search-form');
        this.submitButton = this.formElement.querySelector('input[type="submit"]');
        this.searchDiv = window.document.getElementById('search-results');
        this.tableElement = this.searchDiv.getElementsByTagName('table')[0];
        this.errorElement = this.searchDiv.querySelector('p.error-message');

        this.setup = function() {
            this.formElement.addEventListener('submit', (e) => {
                e.preventDefault();

                //disabling in order to not process multiple requests
                this.disableSubmit();

                if(e.target.checkValidity()) {
                    this.search(e.target.elements['keyword'].value);
                } else {
                    e.target.reportValidity();
                }
            });

            this.searchDiv.style.display = 'none';
        }

        this.search = function(keyword) {
            makeCall('GET', 'Search?keyword=' + keyword, null, (req) => {
                this.enableSubmit();

                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.showSearchResults(response);
                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            this.errorElement.textContent = response.detailMessage;
                        } catch (e) {
                            this.errorElement.textContent = "An error occurred.";
                        }
                }
            });
        }

        this.close = function() {
            this.hide();
            this.clear();
        }

        this.hide = function() {
            this.searchDiv.style.display = 'none';
        }

        this.clear = function() {
            this.errorElement.textContent = '';
            this.tableElement.getElementsByTagName('tbody')[0].innerHTML = '';
        }

        this.disableSubmit = function() {
            this.submitButton.disabled = true;
            this.submitButton.style.cursor = 'wait';
        }

        this.enableSubmit = function() {
            this.submitButton.disabled = false;
            this.submitButton.style.cursor = 'default';
        }

        this.showSearchResults = function(results) {
            this.searchDiv.style.display = 'block';
            if(results.length === 0) {
                this.tableElement.style.display = 'none';
                this.errorElement.textContent = "No results found.";
            } else {
                this.errorElement.textContent = '';
                this.tableElement.style.display = 'table';

                this.clear();

                let tbody = this.tableElement.getElementsByTagName('tbody')[0];

                results.forEach((result) => {
                    let tr = document.createElement('tr');
                    //todo: set attribute itesmMatch
                    let td = document.createElement('td');
                    //td.textContent = result.itemsCodeName + ' (' + result.itemsCodeNameCount + ')';
                    td.textContent = result.itemsCodeName;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent = calculateTimeBetween(new Date(), new Date(result.deadline));
                    tr.appendChild(td);

                    td = document.createElement('td');
                    if(result.maxOffer === 0) {
                        td.textContent = 'Nessuna offerta';
                    } else {
                        td.textContent = result.maxOffer + " $";
                    }
                    tr.appendChild(td);

                    td = document.createElement('td');
                    let a = document.createElement('a');
                    a.href = '#';
                    a.textContent = 'Apri';
                    a.addEventListener('click', (e) => {
                        e.preventDefault();
                        saveViewedAuction(result.id);
                        offersAuctions.load(result.id);
                    });
                    td.appendChild(a);
                    tr.appendChild(td);

                    tbody.appendChild(tr);
                });
            }
        }
    }

    function AuctionDetails(elements) {
        this.div = elements._auctionDiv;
        this.closeButton = this.div.querySelector('.auction-close');
        this.idElement = this.div.querySelector('.auction-id');
        this.statusElement = this.div.querySelector('.auction-status');
        this.startingPriceElement = this.div.querySelector('.auction-starting-price');
        this.deadlineElement = this.div.querySelector('.auction-deadline');
        this.maxOfferElement = this.div.querySelector('.auction-max-offer');
        this.minimumRiseElement = this.div.querySelector('.auction-minimum-rise');
        this.itemsTable = this.div.querySelector('.auction-items-table').querySelector('tbody');
        this.offersTable = this.div.querySelector('.auction-offers-table');
        this.offersTbody = this.offersTable.querySelector('tbody');
        this.updatingMessage = this.div.querySelector('.updating-message');
        this.closeForm = this.div.querySelector('#auction-close-form');
        this.winnerDiv = this.div.querySelector('#auction-won-by');
        this.timeout = null;

        this.setup = function() {
            this.hide();
            this.closeButton.addEventListener('click', (e) => {
                e.preventDefault();

                this.close();
            });


            this.closeForm.addEventListener('submit', (e) => {
                e.preventDefault();

                makeCall('POST', 'CloseAuction', this.closeForm, (req) => {
                    switch (req.status) {
                        case 200:
                            break;
                        default:
                            try {
                                let response = JSON.parse(req.responseText);
                                this.closeForm.querySelector('.error-message').textContent = response.detailMessage;
                            } catch (e) {
                                this.closeForm.querySelector('.error-message').textContent = "An error occurred.";
                            }
                            break;
                    }
                    this.load(this.closeForm.querySelector('input[name="auction_id"]').value);
                    userAuctions.setup();
                });
            });
        }

        this.show = function() {
            this.div.style.display = 'block';
        }

        this.close = function() {
            this.disableTimeout();
            this.hide();
            this.clear();
        }

        this.disableTimeout = function() {
            clearTimeout(this.timeout);
            this.timeout = null;
        }

        this.hide = function() {
            this.div.style.display = 'none';
        }

        this.clear = function() {
            this.idElement.textContent = '';
            this.statusElement.textContent = '';
            this.startingPriceElement.textContent = '';
            this.deadlineElement.textContent = '';
            this.maxOfferElement.textContent = '';
            this.minimumRiseElement.textContent = '';
            this.itemsTable.innerHTML = '';
            this.offersTbody.innerHTML = '';
            this.updatingMessage.style.display = 'none';
            this.closeForm.style.display = 'none';
            this.winnerDiv.style.display = 'none';
        }

        this.load = function(auctionId) {
            makeCall('GET', 'AuctionDetails?auction_id=' + auctionId, null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.clear();
                        this.updateData(response);
                        this.show();
                        if(response.status === 1 && response.expired === true) {
                            this.closeForm.style.display = 'block';
                        }
                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            alert(response.detailMessage);
                        } catch (e) {
                            alert("An error occurred.")
                        }
                }
            });
        }

        this.addItem = function(item) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            let img = document.createElement('img');
            img.src = "GetImage/" + item.imagePath;
            img.alt = item.name;
            td.appendChild(img);
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.code;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.name;
            tr.appendChild(td);

            td = document.createElement('td');
            if (item.description === undefined){
                td.textContent = "N/D";
            } else {
                td.textContent = item.description;
            }
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = item.price + " $" ;
            tr.appendChild(td);

            this.itemsTable.appendChild(tr);
        }

        this.addOffer = function(offer) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            td.textContent = offer.username;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = offer.price + " $";

            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = formatDate(new Date(offer.createdAt));
            tr.appendChild(td);

            this.offersTbody.appendChild(tr);
        }

        this.updateData = function(auction) {
            this.idElement.textContent = auction.id;
            this.statusElement.textContent = (auction.status === 1) ? "Aperta" : "Chiusa";
            this.startingPriceElement.textContent = auction.startingPrice + " $";
            this.deadlineElement.textContent = formatDate(new Date(auction.deadline));
            if(auction.maxOffer === 0) {
                this.maxOfferElement.textContent = "Nessuna offerta";
            } else {
                this.maxOfferElement.textContent = auction.maxOffer + " $";
            }
            this.minimumRiseElement.textContent = auction.minimumRise + " $";

            auction.items.forEach((item) => {
                this.addItem(item);
            });

            if(auction.offers.length > 0) {
                this.div.querySelector('.no-offer-message').style.display = 'none';
                this.offersTable.style.display = 'table';
                auction.offers.forEach((offer) => {
                    this.addOffer(offer);
                });
            } else {
                this.div.querySelector('.no-offer-message').style.display = 'block';
                this.offersTable.style.display = 'none';
            }

            this.closeForm.querySelector('input[name="auction_id"]').value = auction.id;

            if(auction.status === 0) {
                this.winnerDiv.style.display = 'block';

                if(auction.nameBuyer !== undefined) {
                    this.winnerDiv.querySelector('#no-winner').style.display = 'none';
                    this.winnerDiv.querySelector('#winner-details').style.display = 'block';
                    this.winnerDiv.querySelector('#winner-fullname').textContent = auction.nameBuyer;
                    this.winnerDiv.querySelector('#winner-final-price').textContent = auction.maxOffer + " $";
                    this.winnerDiv.querySelector('#winner-address').textContent = auction.shippingAddressBuyer;
                } else {
                    this.winnerDiv.querySelector('#winner-details').style.display = 'none';
                    this.winnerDiv.querySelector('#no-winner').style.display = 'block';
                }
            }
        }
    }

    function WonAuctions(elements) {
        this.div = elements._wonAuctionsDiv;
        this.table = this.div.querySelector('table');
        this.errorElement = this.div.querySelector('p.error-message');
        this.tbody = this.div.querySelector('tbody');

        this.setup = function() {
            makeCall('GET', 'WonAuctions', null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.updateData(response);
                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            alert(response.detailMessage);
                        } catch (e) {
                            alert("An error occurred.")
                        }
                }
            });
        }

        this.clear = function() {
            this.tbody.innerHTML = '';
            this.errorElement.textContent = '';
        }

        this.updateData = function(auctions) {
            this.clear();

            if (auctions.length === 0) {
                this.table.style.display = 'none';
                this.errorElement.textContent = 'Non hai ancora vinto nessuna asta';
            } else {
                this.table.style.display = 'table';
                this.errorElement.textContent = '';
                auctions.forEach((auction) => {
                    this.addAuction(auction);
                });
            }
        }

        this.addAuction = function(auction) {
            auction.items.forEach((item, i) => {
                let tr = document.createElement('tr');

                tr.classList.add('grey');

                let td = document.createElement('td');
                if(i === 0) {
                    td = document.createElement('td');
                    td.textContent = auction.id;
                    td.rowSpan = auction.items.length;
                    tr.appendChild(td);
                }

                td = document.createElement('td');
                td.textContent = item.code;
                tr.appendChild(td);

                td = document.createElement('td');
                let img = document.createElement('img');
                img.src = "GetImage/" + item.imagePath;
                img.alt = item.name;
                td.appendChild(img);
                tr.appendChild(td);

                td = document.createElement('td');
                td.textContent = item.name;
                tr.appendChild(td);

                td = document.createElement('td');
                td.textContent = item.description;
                tr.appendChild(td);

                td = document.createElement('td');
                td.textContent = item.price + " $";
                tr.appendChild(td);

                if(i === 0) {
                    td = document.createElement('td');
                    td.textContent = auction.maxOffer + " $";
                    td.rowSpan = auction.items.length;
                    tr.appendChild(td);
                }

                this.tbody.appendChild(tr);
            });
        }
    }

    function ViewedAuctions(elements) {
        this.div = elements._viewedAuctionsDiv;
        this.table = this.div.querySelector('table');
        this.tbody = this.table.querySelector('tbody');
        this.noRecentlyViewedElement = this.div.querySelector('#no-viewed-auctions');

        this.setup = function() {
            this.clear();
            let auctions = getViewedAuctions();
            if(auctions.length === 0) {
                this.table.style.display = 'none';
                this.noRecentlyViewedElement.style.display = 'block';
            } else {
                this.table.style.display = 'table';
                this.noRecentlyViewedElement.style.display = 'none';
                for (let i = 0; i < auctions.length; i++) {
                    this.loadAuction(auctions[i]);
                }
            }
        }

        this.clear = function() {
            this.tbody.innerHTML = '';
        }

        this.loadAuction = function(auctionID) {
            makeCall('GET', 'OffersAuction?auction_id=' + auctionID, null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);

                        if(response.status === 1) {
                            this.addAuction(response);
                        } else {
                            removeViewedAuction(response.id);
                        }

                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            alert(response.detailMessage);
                        } catch (e) {
                            alert("An error occurred.")
                        }
                }
            });
        }

        this.addAuction = function(auction) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            td.textContent = auction.itemsCodeName;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = formatDate(new Date(auction.deadline));
            tr.appendChild(td);

            td = document.createElement('td');
            if(auction.maxOffer === 0) {
                td.textContent = 'Nessuna offerta';
            } else {
                td.textContent = auction.maxOffer + " $";
            }
            tr.appendChild(td);

            td = document.createElement('td');
            let a = document.createElement('a');
            a.href = '#';
            a.textContent = 'Apri';
            a.addEventListener('click', (e) => {
                e.preventDefault();

                offersAuctions.load(auction.id);
            });
            td.appendChild(a);
            tr.appendChild(td);

            this.tbody.appendChild(tr);
        }
    }

    function UserAuctions(elements) {
        this.div = elements._userAuctionsDiv;
        this.openAuctionsTable = this.div.querySelector('#user-open-auctions');
        this.openAuctionsBody = this.openAuctionsTable.querySelector('tbody');
        this.noOpenAuctions = this.div.querySelector('#no-open-auctions');
        this.closedAuctionsTable = this.div.querySelector('#user-closed-auctions');
        this.closedAuctionsBody = this.closedAuctionsTable.querySelector('tbody');
        this.noClosedAuctions = this.div.querySelector('#no-closed-auctions');

        this.setup = function () {
            this.clear();
            makeCall('GET', 'UserAuctions', null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.updateData(response);
                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            alert(response.detailMessage);
                        } catch (e) {
                            alert("An error occurred.")
                        }
                        return;
                }
            });
        }

        this.clear = function () {
            this.openAuctionsBody.innerHTML = '';
            this.closedAuctionsBody.innerHTML = '';
        }

        this.updateData = function (auctions) {
            let openAuctions = auctions.filter((auction) => {
                return auction.status === 1;
            });
            let closedAuctions = auctions.filter((auction) => {
                return auction.status === 0;
            });

            if (openAuctions.length === 0) {
                this.openAuctionsTable.style.display = 'none';
                this.noOpenAuctions.style.display = 'block';
            } else {
                this.openAuctionsTable.style.display = 'table';
                this.noOpenAuctions.style.display = 'none';
                openAuctions.forEach((auction) => {
                    this.addOpenAuction(auction);
                });
            }

            if (closedAuctions.length === 0) {
                this.closedAuctionsTable.style.display = 'none';
                this.noClosedAuctions.style.display = 'block';
            } else {
                this.closedAuctionsTable.style.display = 'table';
                this.noClosedAuctions.style.display = 'none';
                closedAuctions.forEach((auction) => {
                    this.addClosedAuction(auction);
                });
            }
        }

        this.addOpenAuction = function (auction) {
            let tr = document.createElement('tr');

            let td = document.createElement('td');
            td.textContent = auction.id;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = auction.itemsCodeName;
            tr.appendChild(td);

            td = document.createElement('td');
            if (auction.maxOffer == 0) {
                td.textContent = 'Nessuna offerta';
            } else {
                td.textContent = auction.maxOffer + " $";
            }
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = calculateTimeBetween(new Date(), new Date(auction.deadline));
            tr.appendChild(td);

            td = document.createElement('td');
            let a = document.createElement('a');
            a.href = '#';
            a.textContent = 'Apri';
            a.addEventListener('click', (e) => {
                e.preventDefault();

                auctionDetails.load(auction.id);
            });
            td.appendChild(a);
            tr.appendChild(td);

            this.openAuctionsBody.appendChild(tr);
        }



        this.addClosedAuction = function (auction) {

            let tr = document.createElement('tr');

            let td = document.createElement('td');
            td.textContent = auction.id;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = auction.itemsCodeName;
            tr.appendChild(td);

            td = document.createElement('td');
            if (auction.nameBuyer === undefined) {
                td.textContent = 'Nessun aggiudicatario';
            } else {
                td.textContent = auction.nameBuyer;
            }
            tr.appendChild(td);

            td = document.createElement('td');
            if (auction.maxOffer == 0) {
                td.textContent = 'Nessuna offerta';
            } else {
                td.textContent = auction.maxOffer + " $";
            }
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = formatDate(new Date(auction.deadline));
            tr.appendChild(td);

            td = document.createElement('td');
            let a = document.createElement('a');
            a.href = '#';
            a.textContent = 'Apri';
            a.addEventListener('click', (e) => {
                e.preventDefault();

                auctionDetails.load(auction.id);
            });
            td.appendChild(a);
            tr.appendChild(td);

            this.closedAuctionsBody.appendChild(tr);

        }

    }

    function AddItem(elements) {
        this.form = elements._addItemForm;
        this.errorElement = this.form.querySelector('.error-message');

        this.setup = function () {
            this.form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submit();
            });
        }

        this.submit = function () {
            this.disableSubmit();
            this.errorElement.textContent = '';

            if (this.form.checkValidity()) {
                makeCall("POST", "CreateItem", this.form, (req) => {
                    this.enableSubmit();

                    switch (req.status) {
                        case 200:
                            this.form.reset();
                            newAuction.load();

                            break;
                        default:
                            try {
                                let response = JSON.parse(req.responseText);
                                this.errorElement.textContent = response.detailMessage;
                            } catch (e) {
                                this.errorElement.textContent = "An error occurred.";
                            }
                    }
                });
            } else {
                this.form.reportValidity();
            }
        }

        this.disableSubmit = function() {
            this.form.querySelector('input[type=submit]').disabled = true;
            this.form.querySelector('input[type=submit]').style.cursor = 'wait';
        }

        this.enableSubmit = function() {
            this.form.querySelector('input[type=submit]').disabled = false;
            this.form.querySelector('input[type=submit]').style.cursor = 'default';
        }
    }

    function NewAuction(elements) {
        this.form = elements._newAuctionForm;
        this.errorElement = this.form.querySelector('.error-message');

        this.setup = function () {
            this.form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submit();
            });

            this.form.querySelector('input#new-auction-deadline').min = new Date().toISOString().slice(0, 16);
        }

        this.submit = function () {
            this.disableSubmit();
            this.errorElement.textContent = '';

            if(this.form.querySelectorAll('input[type=checkbox]:checked').length === 0) {
                alert("Seleziona almeno un oggetto.");
                this.enableSubmit();
                return;
            }

            if (this.form.checkValidity()) {
                makeCall("POST", "CreateAuction", this.form, (req) => {
                    this.enableSubmit();

                    switch (req.status) {
                        case 200:
                            this.form.reset();
                            this.load();
                            userAuctions.setup();
                            auctionDetails.load(JSON.parse(req.responseText).id, false);
                            break;
                        default:
                            try {
                                let response = JSON.parse(req.responseText);
                                this.errorElement.textContent = response.detailMessage;
                            } catch (e) {
                                this.errorElement.textContent = "An error occurred.";
                            }
                    }
                });
            } else {
                this.form.reportValidity();
            }
        }

        this.load = function () {
            makeCall("GET", "AvailableItems", null, (req) => {
                switch (req.status) {
                    case 200:
                        let response = JSON.parse(req.responseText);
                        this.updateItems(response);
                        break;
                    default:
                        try {
                            let response = JSON.parse(req.responseText);
                            this.errorElement.textContent = response.message;
                        } catch (e) {
                            this.errorElement.textContent = "An error occurred.";
                        }
                }
            });
        }

        this.updateItems = function (items) {
            let itemsDiv = this.form.querySelector('#items-list');
            itemsDiv.innerHTML = '';

            if(items.length === 0) {
                this.form.querySelector('#no-items-available').style.display = 'block';
            } else {
                this.form.querySelector('#no-items-available').style.display = 'none';
                items.forEach((item) => {
                    let div = document.createElement('div');
                    div.title = item.description;

                    let input = document.createElement('input');
                    input.type = 'checkbox';
                    input.name = 'items';
                    input.value = item.code;
                    input.id = 'sel-item-' + item.code;
                    div.appendChild(input);
                    let label = document.createElement('label');
                    label.htmlFor = 'sel-item-' + item.code;
                    label.textContent = item.name + ' (' + item.price + " $ " + ')';
                    div.appendChild(label);

                    itemsDiv.appendChild(div);
                });
            }
        }

        this.disableSubmit = function() {
            this.form.querySelector('input[type=submit]').disabled = true;
            this.form.querySelector('input[type=submit]').style.cursor = 'wait';
        }

        this.enableSubmit = function() {
            this.form.querySelector('input[type=submit]').disabled = false;
            this.form.querySelector('input[type=submit]').style.cursor = 'default';
        }
    }

    function PageOrchestrator() {
        this.buyDiv = document.getElementById('buy-container');
        this.sellDiv = document.getElementById('sell-container');
        this.buyButton = document.getElementById('buy-navbar-button');
        this.sellButton = document.getElementById('sell-navbar-button');
        this.logoutButton = document.getElementById('logout-navbar-button');

        this.start = function () {
            search.setup();
            auctionDetails.setup();
            addItem.setup();
            newAuction.setup();
            offersAuctions.setup();

            let user_id = localStorage.getItem('user_id');
            let key_saved_auctions = user_id + + "_" +"saved_auctions";
            switch (key_saved_auctions) {
                case 'buy':
                    this.showBuy();
                    break;
                case 'sell':
                    this.showSell();
                    break;
                default:
                    this.showBuy();
                    break;
            }

            this.buyButton.addEventListener('click', (e) => {
                e.preventDefault();
                this.showBuy();
                let user_id = localStorage.getItem('user_id');
                let key_last_action_time = user_id + + "_" +"last_action_time";
                localStorage.setItem(key_last_action_time, 'buy');
            });

            this.sellButton.addEventListener('click', (e) => {
                e.preventDefault();
                this.showSell();
                let user_id = localStorage.getItem('user_id');
                let key_last_action_time = user_id + + "_" +"last_action_time";
                localStorage.setItem(key_last_action_time, 'sell');
            });

            this.logoutButton.addEventListener('click', (e) => {
                logout();
            });

            window.document.getElementById('firstname-navbar').textContent = localStorage.getItem('firstname');
        }

        this.showBuy = function () {
            this.buyDiv.style.display = 'block';
            this.sellDiv.style.display = 'none';
            this.buyButton.classList.add('selected');
            this.sellButton.classList.remove('selected');
            search.close();
            auctionDetails.close();
            offersAuctions.close();
            wonAuctions.setup();
            viewedAuctions.setup();
        }



        this.showSell = function () {
            this.buyDiv.style.display = 'none';
            this.sellDiv.style.display = 'block';
            this.buyButton.classList.remove('selected');
            this.sellButton.classList.add('selected');
            search.close();
            auctionDetails.close();
            offersAuctions.close();
            userAuctions.setup();
            newAuction.load();
        }



    }
}