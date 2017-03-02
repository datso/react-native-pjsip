/* $Id: pjsip.h 3553 2011-05-05 06:14:19Z nanang $ */
/* 
 * Copyright (C) 2008-2011 Teluu Inc. (http://www.teluu.com)
 * Copyright (C) 2003-2008 Benny Prijono <benny@prijono.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
#ifndef __PJSIP_H__
#define __PJSIP_H__

/* Base types. */
#include <VialerPJSIP/pjsip/sip_types.h>
#include <VialerPJSIP/pjsip/sip_errno.h>

/* Messaging and parsing. */
#include <VialerPJSIP/pjsip/sip_uri.h>
#include <VialerPJSIP/pjsip/sip_tel_uri.h>
#include <VialerPJSIP/pjsip/sip_msg.h>
#include <VialerPJSIP/pjsip/sip_multipart.h>
#include <VialerPJSIP/pjsip/sip_parser.h>

/* Core */
#include <VialerPJSIP/pjsip/sip_event.h>
#include <VialerPJSIP/pjsip/sip_module.h>
#include <VialerPJSIP/pjsip/sip_endpoint.h>
#include <VialerPJSIP/pjsip/sip_util.h>

/* Transport layer */
#include <VialerPJSIP/pjsip/sip_transport.h>
#include <VialerPJSIP/pjsip/sip_transport_udp.h>
#include <VialerPJSIP/pjsip/sip_transport_loop.h>
#include <VialerPJSIP/pjsip/sip_transport_tcp.h>
#include <VialerPJSIP/pjsip/sip_transport_tls.h>
#include <VialerPJSIP/pjsip/sip_resolve.h>

/* Authentication. */
#include <VialerPJSIP/pjsip/sip_auth.h>
#include <VialerPJSIP/pjsip/sip_auth_aka.h>

/* Transaction layer. */
#include <VialerPJSIP/pjsip/sip_transaction.h>

/* UA Layer. */
#include <VialerPJSIP/pjsip/sip_ua_layer.h>
#include <VialerPJSIP/pjsip/sip_dialog.h>


#endif	/* __PJSIP_H__ */

