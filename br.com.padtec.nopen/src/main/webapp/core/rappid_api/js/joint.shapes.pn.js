/*! Rappid - the diagramming toolkit

Copyright (c) 2014 client IO

 2015-02-04 


This Source Code Form is subject to the terms of the Rappid Academic License
, v. 1.0. If a copy of the Rappid License was not distributed with this
file, You can obtain one at http://jointjs.com/license/rappid_academic_v1.txt
 or from the Rappid archive as was distributed by client IO. See the LICENSE file.*/


if (typeof exports === 'object') {

    var joint = {
        util: require('../src/core').util,
        shapes: {
            basic: require('./joint.shapes.basic')
        },
        dia: {
            ElementView: require('../src/joint.dia.element').ElementView,
            Link: require('../src/joint.dia.link').Link
        }
    };
}

joint.shapes.pn = {};

joint.shapes.pn.Place = joint.shapes.basic.Generic.extend({

    markup: '<g class="rotatable"><g class="scalable"><circle class="root"/><g class="tokens" /></g><text class="label"/></g>',

    defaults: joint.util.deepSupplement({

        type: 'pn.Place',
        size: { width: 50, height: 50 },
        attrs: {
            '.root': {
                r: 25,
                fill: '#ffffff',
                stroke: '#000000',
                transform: 'translate(25, 25)'
            },
            '.label': {
                'text-anchor': 'middle',
                'ref-x': .5,
                'ref-y': -20,
                ref: '.root',
                fill: '#000000',
                'font-size': 12
            },
            '.tokens > circle': {
                fill: '#000000',
                r: 5
            },
            '.tokens.one > circle': { transform: 'translate(25, 25)' },
            
            '.tokens.two > circle:nth-child(1)': { transform: 'translate(19, 25)' },
            '.tokens.two > circle:nth-child(2)': { transform: 'translate(31, 25)' },
            
            '.tokens.three > circle:nth-child(1)': { transform: 'translate(18, 29)' },
            '.tokens.three > circle:nth-child(2)': { transform: 'translate(25, 19)' },
            '.tokens.three > circle:nth-child(3)': { transform: 'translate(32, 29)' },

            '.tokens.alot > text': {
		transform: 'translate(25, 18)',
		'text-anchor': 'middle',
                fill: '#000000'
            }
        }

    }, joint.shapes.basic.Generic.prototype.defaults)
});


joint.shapes.pn.PlaceView = joint.dia.ElementView.extend({

    initialize: function() {

        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

        this.model.on('change:tokens', function() {

            this.renderTokens();
            this.update();

        }, this);
    },

    render: function() {

        joint.dia.ElementView.prototype.render.apply(this, arguments);

        this.renderTokens();
        this.update();
    },

    renderTokens: function() {

        var $tokens = this.$('.tokens').empty();
        $tokens[0].className.baseVal = 'tokens';

        var tokens = this.model.get('tokens');

        if (!tokens) return;

        switch (tokens) {

          case 1:
            $tokens[0].className.baseVal += ' one';
            $tokens.append(V('<circle/>').node);
            break;
            
          case 2:
            $tokens[0].className.baseVal += ' two';
            $tokens.append(V('<circle/>').node, V('<circle/>').node);
            break;

          case 3:
            $tokens[0].className.baseVal += ' three';
            $tokens.append(V('<circle/>').node, V('<circle/>').node, V('<circle/>').node);
            break;

          default:
            $tokens[0].className.baseVal += ' alot';
            $tokens.append(V('<text/>').text(tokens + '' ).node);
            break;
        }
    }
});


joint.shapes.pn.Transition = joint.shapes.basic.Generic.extend({

    markup: '<g class="rotatable"><g class="scalable"><rect class="root"/></g></g><text class="label"/>',

    defaults: joint.util.deepSupplement({

        type: 'pn.Transition',
        size: { width: 12, height: 50 },
        attrs: {
            'rect': {
                width: 12,
                height: 50,
                fill: '#000000',
                stroke: '#000000'
            },
            '.label': {
                'text-anchor': 'middle',
                'ref-x': .5,
                'ref-y': -20,
                ref: 'rect',
                fill: '#000000',
                'font-size': 12
            }
        }

    }, joint.shapes.basic.Generic.prototype.defaults)
});

joint.shapes.pn.Link = joint.dia.Link.extend({

    defaults: joint.util.deepSupplement({

        attrs: { '.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' }}
        
    }, joint.dia.Link.prototype.defaults)
});

if (typeof exports === 'object') {

    module.exports = joint.shapes.pn;
}
